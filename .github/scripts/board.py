#!/usr/bin/env python3
"""Помощник доски задач (GitHub Projects): общий для workflows и локальной работы.

Команды (флаг --dry-run перед командой печатает изменения, но не вносит их):
  add <issue> [--priority P0..P3] [--status Backlog|In Progress|In Review|Done]
  move <status> (--issue N | --branch REF) [--only-from S1,S2]
  priority <issue> <P0..P3>
  pr-sync --pr N      состояние карточки и строка Closes по PR из ветки FM-N-…
  digest [--github-output]   недельная сводка доски; текст печатается в stdout

Токены. Операции с доской идут с BOARD_TOKEN (classic PAT со scope project): токен Actions
не может менять доски пользователя. Если переменной нет, берётся вход gh. Всё остальное
(чтение и правка PR) — с GH_TOKEN. Как это устроено — docs/task-tracking.md.
"""
from __future__ import annotations

import argparse
import json
import os
import re
import subprocess
import sys
import time
import uuid
from dataclasses import dataclass
from datetime import datetime, timezone
from typing import Optional

OWNER = os.environ.get("BOARD_OWNER", "IliaVoskoboinikov")
PROJECT_NUMBER = int(os.environ.get("BOARD_PROJECT", "5"))
REPO = os.environ.get("GITHUB_REPOSITORY", "IliaVoskoboinikov/Finance-Manager")
REPO_OWNER, REPO_NAME = REPO.split("/", 1)
PROJECT_URL = f"https://github.com/users/{OWNER}/projects/{PROJECT_NUMBER}"

STATUSES = ("Backlog", "In Progress", "In Review", "Done")
PRIORITIES = ("P0", "P1", "P2", "P3")
FLAG_LABELS = ("blocked", "needs-backend", "needs-decision")
EPIC_LABEL = "epic"
STALE_DAYS = 7
CLOSED_WINDOW_DAYS = 7
# Лимит Telegram на сообщение — 4096 символов; запас на многоточие.
TELEGRAM_LIMIT = 3900

BRANCH_RE = re.compile(r"^FM-(\d+)(?:-|$)")
# Эти ветки заводят не по задачам доски, замечание про имя им не нужно.
IGNORED_BRANCH_PREFIXES = ("renovate/", "releases/", "tests/", "dependabot/")
CLOSING_RE = re.compile(
    r"(?i)\b(?:close[sd]?|fix(?:es|ed)?|resolve[sd]?)\s*:?\s+(?:[\w.-]+/[\w.-]+)?#(\d+)\b"
)
# Незаполненная строка из шаблона PR.
EMPTY_CLOSES_RE = re.compile(r"(?mi)^Closes #[ \t]*$")
TRANSIENT_ERRORS = ("timeout", "tls handshake", "connection reset", "eof", "502", "503", "504")

Q_BOARD = """
query($owner: String!, $number: Int!) {
  user(login: $owner) {
    projectV2(number: $number) {
      id
      fields(first: 50) { nodes { ... on ProjectV2SingleSelectField { id name options { id name } } } }
    }
  }
}"""

Q_ISSUE = """
query($owner: String!, $repo: String!, $number: Int!) {
  repository(owner: $owner, name: $repo) {
    issue(number: $number) {
      id
      title
      state
      projectItems(first: 20) {
        nodes {
          id
          project { url }
          status: fieldValueByName(name: "Status") {
            ... on ProjectV2ItemFieldSingleSelectValue { name }
          }
          priority: fieldValueByName(name: "Priority") {
            ... on ProjectV2ItemFieldSingleSelectValue { name }
          }
        }
      }
    }
  }
}"""

M_ADD = """
mutation($project: ID!, $content: ID!) {
  addProjectV2ItemById(input: {projectId: $project, contentId: $content}) { item { id } }
}"""

M_SET = """
mutation($project: ID!, $item: ID!, $field: ID!, $option: String!) {
  updateProjectV2ItemFieldValue(input: {
    projectId: $project, itemId: $item, fieldId: $field, value: {singleSelectOptionId: $option}
  }) { projectV2Item { id } }
}"""

Q_CARDS = """
query($owner: String!, $number: Int!, $after: String) {
  user(login: $owner) {
    projectV2(number: $number) {
      items(first: 100, after: $after) {
        pageInfo { hasNextPage endCursor }
        nodes {
          status: fieldValueByName(name: "Status") {
            ... on ProjectV2ItemFieldSingleSelectValue { name updatedAt }
          }
          priority: fieldValueByName(name: "Priority") {
            ... on ProjectV2ItemFieldSingleSelectValue { name }
          }
          content {
            ... on Issue {
              number
              title
              state
              closedAt
              labels(first: 20) { nodes { name } }
              subIssuesSummary { total completed }
            }
          }
        }
      }
    }
  }
}"""


class BoardError(RuntimeError):
    """Ошибка обращения к GitHub или неверные входные данные."""


@dataclass
class Board:
    """Идентификаторы доски: сам проект, поля и варианты их значений."""

    project_id: str
    fields: dict  # имя поля -> (id поля, {имя варианта: id варианта})


@dataclass
class Issue:
    """Issue и его карточка на доске (если уже добавлена)."""

    number: int
    node_id: str
    title: str
    state: str
    item_id: Optional[str]
    status: Optional[str]
    priority: Optional[str]


@dataclass
class Card:
    """Карточка доски в виде, нужном для сводки."""

    number: int
    title: str
    state: str
    status: Optional[str]
    priority: Optional[str]
    labels: frozenset
    # Когда последний раз менялся Status: время issue сдвигают и комментарии, и лейблы,
    # а «застрявшая» карточка — та, что давно не меняла колонку.
    status_changed_at: datetime
    closed_at: Optional[datetime]
    sub_total: int
    sub_done: int

    @property
    def key(self) -> str:
        return f"FM-{self.number}"

    @property
    def is_epic(self) -> bool:
        return EPIC_LABEL in self.labels


# --- Обращение к GitHub -------------------------------------------------------------------


def run_gh(args, token=None, stdin=None, attempts=3) -> str:
    """Запускает gh; повторяет только сетевые сбои (на таймауты TLS у GitHub натыкались не раз)."""
    env = dict(os.environ)
    if token:
        env["GH_TOKEN"] = token
    error = ""
    for attempt in range(1, attempts + 1):
        proc = subprocess.run(["gh", *args], capture_output=True, text=True, env=env, input=stdin)
        if proc.returncode == 0:
            return proc.stdout
        error = (proc.stderr or proc.stdout).strip()
        transient = any(marker in error.lower() for marker in TRANSIENT_ERRORS)
        if attempt == attempts or not transient:
            break
        time.sleep(2 * attempt)
    raise BoardError(f"gh {' '.join(args[:3])}: {error[:300]}")


def graphql(query: str, **variables):
    """GraphQL-запрос с токеном доски; возвращает поле data."""
    args = ["api", "graphql", "-f", f"query={query}"]
    for name, value in variables.items():
        if value is None:
            continue
        args += ["-F" if isinstance(value, int) else "-f", f"{name}={value}"]
    return json.loads(run_gh(args, token=os.environ.get("BOARD_TOKEN")))["data"]


def load_board() -> Board:
    project = graphql(Q_BOARD, owner=OWNER, number=PROJECT_NUMBER)["user"]["projectV2"]
    if project is None:
        raise BoardError(f"Доска {PROJECT_URL} недоступна: проверьте токен (нужен scope project)")
    fields = {
        node["name"]: (node["id"], {o["name"]: o["id"] for o in node["options"]})
        for node in project["fields"]["nodes"]
        if node.get("options") is not None
    }
    return Board(project["id"], fields)


def get_issue(number: int) -> Optional[Issue]:
    data = graphql(Q_ISSUE, owner=REPO_OWNER, repo=REPO_NAME, number=number)["repository"]["issue"]
    if data is None:
        return None
    item = next(
        (n for n in data["projectItems"]["nodes"] if n["project"]["url"] == PROJECT_URL), None
    )
    status = ((item or {}).get("status") or {}).get("name")
    priority = ((item or {}).get("priority") or {}).get("name")
    return Issue(
        number, data["id"], data["title"], data["state"], item and item["id"], status, priority
    )


def require_issue(number: int) -> Issue:
    issue = get_issue(number)
    if issue is None:
        raise BoardError(f"Issue #{number} не найден в {REPO}")
    return issue


def ensure_item(board: Board, issue: Issue, dry_run: bool) -> str:
    """Возвращает id карточки issue, при необходимости добавляя её на доску."""
    if issue.item_id:
        return issue.item_id
    if dry_run:
        print(f"[dry-run] добавить #{issue.number} на доску")
        return "dry-run-item"
    return graphql(M_ADD, project=board.project_id, content=issue.node_id)["addProjectV2ItemById"][
        "item"
    ]["id"]


def set_single_select(board: Board, item_id: str, field: str, option: str, dry_run: bool) -> None:
    if field not in board.fields:
        raise BoardError(f"На доске нет поля {field}")
    field_id, options = board.fields[field]
    if option not in options:
        raise BoardError(f"У поля {field} нет значения {option}; есть: {', '.join(options)}")
    if dry_run:
        print(f"[dry-run] {field} = {option}")
        return
    graphql(M_SET, project=board.project_id, item=item_id, field=field_id, option=options[option])


# --- Команды ------------------------------------------------------------------------------


def issue_from_branch(ref: str) -> Optional[int]:
    """Номер задачи из имени ветки FM-42-fix_navigation; для остальных веток None."""
    match = BRANCH_RE.match(ref.removeprefix("refs/heads/"))
    return int(match.group(1)) if match else None


def move_issue(number: int, status: str, only_from=(), dry_run=False) -> str:
    """Переводит карточку в статус; с only_from трогает её, только если она сейчас в одном из них."""
    issue = require_issue(number)
    if issue.state != "OPEN":
        return f"#{number} закрыт — карточку не трогаю"
    current = issue.status or "Backlog"
    if only_from and current not in only_from:
        return f"#{number} в статусе {current}, а не {'/'.join(only_from)} — пропускаю"
    if current == status:
        return f"#{number} уже в статусе {status}"
    board = load_board()
    item_id = ensure_item(board, issue, dry_run)
    set_single_select(board, item_id, "Status", status, dry_run)
    return f"#{number}: {current} → {status}"


def set_priority(number: int, priority: str, dry_run=False) -> str:
    issue = require_issue(number)
    board = load_board()
    item_id = ensure_item(board, issue, dry_run)
    set_single_select(board, item_id, "Priority", priority, dry_run)
    return f"#{number}: приоритет {priority}"


def add_issue(number: int, priority=None, status=None, dry_run=False) -> str:
    """Кладёт issue на доску (повторный вызов безопасен) и при желании ставит поля."""
    issue = require_issue(number)
    board = load_board()
    item_id = ensure_item(board, issue, dry_run)
    if status:
        set_single_select(board, item_id, "Status", status, dry_run)
    if priority:
        set_single_select(board, item_id, "Priority", priority, dry_run)
    return f"#{number} на доске" + (f", Status {status}" if status else "") + (
        f", Priority {priority}" if priority else ""
    )


def ensure_on_board(number: int, default_priority: str, dry_run=False) -> str:
    """Карточка issue есть на доске, а приоритет задан; уже выставленный приоритет не трогает.

    Повторный вызов чинит то, что не доделал прошлый (например, issue завели, а до доски
    не дошли), и не перетирает приоритет, который человек поменял руками.
    """
    issue = require_issue(number)
    if issue.item_id and issue.priority:
        return f"#{number} уже на доске с приоритетом {issue.priority}"
    board = load_board()
    item_id = ensure_item(board, issue, dry_run)
    set_single_select(board, item_id, "Priority", default_priority, dry_run)
    return f"#{number} на доске, Priority {default_priority}"


def ensure_closes(body: str, number: int) -> Optional[str]:
    """Новый текст описания PR со строкой Closes #N или None, если она уже есть."""
    if number in {int(n) for n in CLOSING_RE.findall(body)}:
        return None
    if EMPTY_CLOSES_RE.search(body):
        return EMPTY_CLOSES_RE.sub(f"Closes #{number}", body, count=1)
    return f"Closes #{number}\n\n{body}" if body.strip() else f"Closes #{number}\n"


def pr_target_status(state: str, is_draft: bool) -> Optional[str]:
    """Статус карточки по состоянию PR; смерженный PR двигают встроенные workflows доски."""
    if state == "MERGED":
        return None
    return "In Progress" if state == "CLOSED" or is_draft else "In Review"


def escape_command(text: str) -> str:
    """Экранирование для workflow-команд: имя ветки не должно уметь вставить свою команду."""
    return text.replace("%", "%25").replace("\r", "%0D").replace("\n", "%0A")


def notice(message: str) -> None:
    print(f"::notice title=Доска задач::{escape_command(message)}")
    summary = os.environ.get("GITHUB_STEP_SUMMARY")
    if summary:
        with open(summary, "a", encoding="utf-8") as out:
            out.write(f"- {message}\n")


def pr_sync(number: int, dry_run=False) -> None:
    fields = "headRefName,isDraft,state,body"
    pr = json.loads(run_gh(["pr", "view", str(number), "--repo", REPO, "--json", fields]))
    ref = pr["headRefName"]
    issue_number = issue_from_branch(ref)
    if issue_number is None:
        if not ref.startswith(IGNORED_BRANCH_PREFIXES):
            notice(f"PR #{number}: ветка {ref} не по схеме FM-N-…, задача на доске не обновлена")
        return
    issue = get_issue(issue_number)
    if issue is None:
        notice(f"PR #{number}: задачи #{issue_number} из имени ветки {ref} нет в {REPO}")
        return

    if pr["state"] == "OPEN":
        new_body = ensure_closes(pr["body"] or "", issue_number)
        if new_body is not None:
            if dry_run:
                print(f"[dry-run] дописать Closes #{issue_number} в описание PR #{number}")
            else:
                edit = ["pr", "edit", str(number), "--repo", REPO, "--body-file", "-"]
                run_gh(edit, stdin=new_body)
                print(f"PR #{number}: дописал Closes #{issue_number}")

    status = pr_target_status(pr["state"], pr["isDraft"])
    if status is None:
        print(f"PR #{number} смержен: карточку переводят встроенные workflows доски")
        return
    print(move_issue(issue_number, status, dry_run=dry_run))


# --- Сводка -------------------------------------------------------------------------------


def now_utc() -> datetime:
    return datetime.now(timezone.utc)


def parse_ts(value: Optional[str]) -> Optional[datetime]:
    if not value:
        return None
    return datetime.strptime(value.replace("Z", "+0000"), "%Y-%m-%dT%H:%M:%S%z")


def fetch_cards() -> list:
    cards, after = [], None
    while True:
        data = graphql(Q_CARDS, owner=OWNER, number=PROJECT_NUMBER, after=after)
        items = data["user"]["projectV2"]["items"]
        for node in items["nodes"]:
            content = node.get("content") or {}
            if "number" not in content:  # PR или черновик — на нашей доске их не бывает
                continue
            summary = content.get("subIssuesSummary") or {}
            status = node.get("status") or {}
            cards.append(
                Card(
                    number=content["number"],
                    title=content["title"],
                    state=content["state"],
                    status=status.get("name"),
                    priority=(node.get("priority") or {}).get("name"),
                    labels=frozenset(n["name"] for n in content["labels"]["nodes"]),
                    status_changed_at=parse_ts(status.get("updatedAt")) or now_utc(),
                    closed_at=parse_ts(content.get("closedAt")),
                    sub_total=summary.get("total", 0),
                    sub_done=summary.get("completed", 0),
                )
            )
        if not items["pageInfo"]["hasNextPage"]:
            return cards
        after = items["pageInfo"]["endCursor"]


def shorten(title: str, limit: int = 60) -> str:
    return title if len(title) <= limit else title[: limit - 1] + "…"


def describe(cards) -> str:
    return " · ".join(f"{c.key} {shorten(c.title)}" for c in sorted(cards, key=lambda c: c.number))


def priority_rank(card: Card) -> tuple:
    rank = PRIORITIES.index(card.priority) if card.priority in PRIORITIES else len(PRIORITIES)
    return rank, card.number


def build_digest(cards, now: datetime) -> str:
    """Недельная сводка доски обычным текстом: без разметки, чтобы названия не ломали Telegram."""
    open_cards = [c for c in cards if c.state == "OPEN"]
    work = [c for c in open_cards if not c.is_epic]
    in_progress = [c for c in work if c.status == "In Progress"]
    in_review = [c for c in work if c.status == "In Review"]
    backlog = [c for c in work if (c.status or "Backlog") == "Backlog"]
    stale = [c for c in in_progress if (now - c.status_changed_at).days >= STALE_DAYS]
    startable = sorted((c for c in backlog if not c.labels & set(FLAG_LABELS)), key=priority_rank)
    p0 = [c for c in backlog if c.priority == "P0"]
    no_priority = [c for c in work if c.priority is None]
    flagged = [c for c in work if c.labels & set(FLAG_LABELS)]
    closed = [
        c
        for c in cards
        if c.state == "CLOSED"
        and not c.is_epic
        and c.closed_at
        and (now - c.closed_at).days < CLOSED_WINDOW_DAYS
    ]

    lines = [f"📋 Доска — неделя {now.isocalendar()[1]}"]
    if in_progress:
        lines.append(f"🔨 В работе ({len(in_progress)}): {describe(in_progress)}")
    else:
        lines.append("🔨 В работе: ничего")
    if in_review:
        lines.append(f"👀 На ревью ({len(in_review)}): {describe(in_review)}")
    if stale:
        aged = ", ".join(f"{c.key} ({(now - c.status_changed_at).days} дн.)" for c in stale)
        lines.append(f"⏳ В In Progress {STALE_DAYS}+ дней: {aged}")
    if startable:
        top = startable[0]
        label = top.priority or "без приоритета"
        lines.append(f"➡️ Дальше: {top.key} {shorten(top.title)} ({label})")
    if p0:
        lines.append(f"🔴 P0 в Backlog: {len(p0)}")
    if no_priority:
        lines.append(f"🆕 Без приоритета: {len(no_priority)} — пора разобрать")
    if flagged:
        counts = ", ".join(
            f"{label}: {sum(1 for c in flagged if label in c.labels)}"
            for label in FLAG_LABELS
            if any(label in c.labels for c in flagged)
        )
        lines.append(f"⛔ Заблокировано: {len(flagged)} ({counts})")
    for epic in sorted((c for c in open_cards if c.is_epic), key=lambda c: c.number):
        lines.append(f"🧩 {epic.key} {shorten(epic.title)} — {epic.sub_done}/{epic.sub_total}")
    lines.append(f"✅ Закрыто за неделю: {len(closed)}")
    lines.append(f"Доска: {PROJECT_URL}")

    text = "\n".join(lines)
    return text if len(text) <= TELEGRAM_LIMIT else text[: TELEGRAM_LIMIT - 1] + "…"


def write_github_output(name: str, value: str) -> None:
    path = os.environ.get("GITHUB_OUTPUT")
    if not path:
        raise BoardError("GITHUB_OUTPUT не задан: --github-output работает только в Actions")
    delimiter = f"ghadelimiter_{uuid.uuid4().hex}"
    with open(path, "a", encoding="utf-8") as out:
        out.write(f"{name}<<{delimiter}\n{value}\n{delimiter}\n")


# --- Командная строка ---------------------------------------------------------------------


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Помощник доски задач GitHub Projects")
    parser.add_argument("--dry-run", action="store_true", help="печатать изменения, не вносить их")
    commands = parser.add_subparsers(dest="command", required=True)

    add = commands.add_parser("add", help="положить issue на доску")
    add.add_argument("issue", type=int)
    add.add_argument("--priority", choices=PRIORITIES)
    add.add_argument("--status", choices=STATUSES)

    move = commands.add_parser("move", help="перевести карточку в статус")
    move.add_argument("status", choices=STATUSES)
    target = move.add_mutually_exclusive_group(required=True)
    target.add_argument("--issue", type=int)
    target.add_argument("--branch", help="ветка FM-N-…; для других веток команда ничего не делает")
    move.add_argument("--only-from", default="", help="статусы через запятую, откуда разрешён переход")

    priority = commands.add_parser("priority", help="поставить приоритет")
    priority.add_argument("issue", type=int)
    priority.add_argument("priority", choices=PRIORITIES)

    sync = commands.add_parser("pr-sync", help="синхронизировать карточку с PR")
    sync.add_argument("--pr", type=int, required=True)

    digest = commands.add_parser("digest", help="недельная сводка доски")
    digest.add_argument("--github-output", action="store_true", help="записать текст в $GITHUB_OUTPUT")
    return parser


def run(args) -> None:
    if args.command == "add":
        print(add_issue(args.issue, args.priority, args.status, args.dry_run))
    elif args.command == "move":
        number = args.issue if args.issue else issue_from_branch(args.branch)
        if number is None:
            print(f"Ветка {args.branch} не по схеме FM-N-… — карточку не трогаю")
            return
        only_from = tuple(s.strip() for s in args.only_from.split(",") if s.strip())
        print(move_issue(number, args.status, only_from, args.dry_run))
    elif args.command == "priority":
        print(set_priority(args.issue, args.priority, args.dry_run))
    elif args.command == "pr-sync":
        pr_sync(args.pr, args.dry_run)
    elif args.command == "digest":
        text = build_digest(fetch_cards(), now_utc())
        print(text)
        if args.github_output:
            write_github_output("text", text)


def main(argv=None) -> int:
    args = build_parser().parse_args(argv)
    try:
        run(args)
    except BoardError as error:
        if os.environ.get("GITHUB_ACTIONS"):
            print(f"::error title=Доска задач::{escape_command(str(error))}")
        print(f"Ошибка: {error}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
