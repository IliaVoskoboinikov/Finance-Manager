#!/usr/bin/env python3
"""Красный master → issue: заводит, дополняет и закрывает задачу с лейблом ci-failure.

По одному прогону workflow на master:
  * упал (failure, timed_out) — заводит issue «Красный master: <workflow>» с лейблами
    bug, ci-failure, area:ci, кладёт его на доску с приоритетом P1; если такой issue уже
    открыт, дописывает комментарий со ссылкой на прогон;
  * прошёл (success) — закрывает открытый issue этого workflow;
  * остальное (cancelled, skipped…) игнорирует.

Решает только самый свежий прогон workflow на master. Прогоны соседних коммитов идут
параллельно и могут закончиться в любом порядке: без этой проверки поздно завершившийся
старый зелёный прогон закрыл бы issue, хотя master уже снова красный (и наоборот).

Issue заводят и правят с GH_TOKEN, доску — через board.py (BOARD_TOKEN).
Использование: ci_failure.py --run-id N [--dry-run] [--force]
"""
from __future__ import annotations

import argparse
import json
import os
import sys

import board

DEFAULT_BRANCH = os.environ.get("DEFAULT_BRANCH", "master")
LABELS = ("bug", "ci-failure", "area:ci")
FAILED = ("failure", "timed_out")
FAILED_JOB_CONCLUSIONS = ("failure", "timed_out")


def issue_title(workflow: str) -> str:
    return f"Красный master: {workflow}"


def first_line(text: str) -> str:
    return (text or "").strip().splitlines()[0] if (text or "").strip() else ""


def failed_jobs_markdown(jobs) -> str:
    names = [j["name"] for j in jobs if j.get("conclusion") in FAILED_JOB_CONCLUSIONS]
    return "\n".join(f"- {name}" for name in names) if names else "- не удалось определить"


def issue_body(run: dict, jobs, repo: str) -> str:
    sha = run["head_sha"]
    commit = f"[{sha[:7]}](https://github.com/{repo}/commit/{sha})"
    subject = first_line((run.get("head_commit") or {}).get("message", ""))
    return (
        "### Что происходит\n\n"
        f"Прогон [{run['name']} #{run['run_number']}]({run['html_url']}) на `{DEFAULT_BRANCH}` "
        f"завершился с результатом `{run['conclusion']}`.\n\n"
        f"Коммит: {commit}" + (f" — {subject}" if subject else "") + "\n\n"
        f"Упавшие джобы:\n{failed_jobs_markdown(jobs)}\n\n"
        "### Как воспроизвести\n\n"
        "Открыть прогон по ссылке или перезапустить его; локально — задача Gradle упавшей джобы.\n\n"
        "### Ожидание\n\n"
        f"`{DEFAULT_BRANCH}` зелёный. Issue закроется сам, когда прогон «{run['name']}» "
        "на нём снова пройдёт.\n\n"
        "### Источник\n\n"
        "Автоматика ci-failure (`.github/workflows/ci-failure.yml`)."
    )


def comment_body(run: dict, jobs) -> str:
    return (
        f"Снова красный: [{run['name']} #{run['run_number']}]({run['html_url']}) "
        f"(`{run['conclusion']}`).\n\nУпавшие джобы:\n{failed_jobs_markdown(jobs)}"
    )


def find_open_issue(title: str) -> int | None:
    out = board.run_gh(
        ["issue", "list", "--repo", board.REPO, "--label", "ci-failure", "--state", "open",
         "--limit", "50", "--json", "number,title"]
    )
    return next((i["number"] for i in json.loads(out) if i["title"] == title), None)


def report_red(run: dict, jobs, dry_run: bool) -> None:
    title = issue_title(run["name"])
    existing = find_open_issue(title)
    if existing:
        print(f"Issue #{existing} уже открыт — дописываю комментарий")
        if not dry_run:
            board.run_gh(
                ["issue", "comment", str(existing), "--repo", board.REPO, "--body-file", "-"],
                stdin=comment_body(run, jobs),
            )
        # Прошлый прогон мог завести issue, но не дойти до доски.
        print(board.ensure_on_board(existing, "P1", dry_run))
        return
    body = issue_body(run, jobs, board.REPO)
    if dry_run:
        print(f"[dry-run] завести issue «{title}» с лейблами {', '.join(LABELS)}, на доску с P1")
        print(body)
        return
    args = ["issue", "create", "--repo", board.REPO, "--title", title, "--body-file", "-"]
    for label in LABELS:
        args += ["--label", label]
    url = board.run_gh(args, stdin=body).strip()
    number = int(url.rsplit("/", 1)[-1])
    print(f"Завёл issue #{number}: {url}")
    print(board.ensure_on_board(number, "P1"))


def report_green(run: dict, dry_run: bool) -> None:
    title = issue_title(run["name"])
    existing = find_open_issue(title)
    if not existing:
        print("Открытого issue нет — делать нечего")
        return
    comment = f"Починено: [{run['name']} #{run['run_number']}]({run['html_url']}) снова зелёный."
    print(f"Закрываю issue #{existing}")
    if not dry_run:
        board.run_gh(
            ["issue", "close", str(existing), "--repo", board.REPO, "--comment", comment]
        )


def is_latest_run(run: dict, latest) -> bool:
    """Прогон — самый свежий для своего workflow на master (или новее нет)."""
    return latest is None or latest["id"] == run["id"]


def latest_master_run(run: dict):
    """Самый свежий push-прогон того же workflow на master, в том числе ещё идущий."""
    query = f"branch={DEFAULT_BRANCH}&event=push&per_page=1"
    out = board.run_gh(
        ["api", f"repos/{board.REPO}/actions/workflows/{run['workflow_id']}/runs?{query}"]
    )
    runs = json.loads(out)["workflow_runs"]
    return runs[0] if runs else None


def run(run_id: int, dry_run: bool, force: bool = False) -> None:
    data = json.loads(board.run_gh(["api", f"repos/{board.REPO}/actions/runs/{run_id}"]))
    if data["head_branch"] != DEFAULT_BRANCH:
        print(f"Прогон на ветке {data['head_branch']}, не на {DEFAULT_BRANCH} — пропускаю")
        return
    latest = None if force else latest_master_run(data)
    if not is_latest_run(data, latest):
        print(
            f"Есть более свежий прогон {data['name']} #{latest['run_number']} "
            f"({latest['status']}) — этот устарел, решать будет свежий"
        )
        return
    conclusion = data["conclusion"]
    if conclusion in FAILED:
        jobs_out = board.run_gh(
            ["api", f"repos/{board.REPO}/actions/runs/{run_id}/jobs", "--paginate",
             "--jq", ".jobs[] | {name, conclusion}"]
        )
        jobs = [json.loads(line) for line in jobs_out.splitlines() if line.strip()]
        report_red(data, jobs, dry_run)
    elif conclusion == "success":
        report_green(data, dry_run)
    else:
        print(f"Результат {conclusion} не требует действий")


def main(argv=None) -> int:
    parser = argparse.ArgumentParser(description="Красный master → issue с лейблом ci-failure")
    parser.add_argument("--run-id", type=int, required=True)
    parser.add_argument("--dry-run", action="store_true", help="печатать действия, не выполнять")
    parser.add_argument(
        "--force", action="store_true", help="не проверять, что прогон самый свежий (ручная проверка)"
    )
    args = parser.parse_args(argv)
    try:
        run(args.run_id, args.dry_run, args.force)
    except board.BoardError as error:
        print(f"Ошибка: {error}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
