---
name: board
description: Work with the Finance-Manager task board (GitHub Projects) — propose and create a card by the project template, take a task into work, move a card, show the board digest, link sub-issues. Use when the user asks to create/file a task, card or issue, says "возьми FM-N" / "заведи задачу" / "что на доске", or when a bug or follow-up outside the current task should be recorded.
---

# Task board

Rules are in `docs/agents/task-board.md`, the process in `docs/task-tracking.md`. This skill
holds the exact commands. Board commands go through `.github/scripts/board.py` (it resolves
the board, fields and options by name — no ids needed); prefix any command with `--dry-run`
to see the changes without making them.

## Show the board

```bash
python3 .github/scripts/board.py digest
```

Use it before starting a task: the «🔨 В работе» line shows how many tasks are in progress
(epics are not counted there).

## Create a card

1. Duplicates: `gh issue list --repo IliaVoskoboinikov/Finance-Manager --state all --search "<keywords>"`.
2. Draft in chat and wait for an explicit "yes":

   ```
   Заголовок: Починить балансы при смене счёта в редактировании операции
   Лейблы: bug, area:data
   Приоритет: P1
   Тело:
   ### Что происходит
   …
   ```

3. Write the body to a scratch file and create the issue. Labels: exactly one type, one or
   more `area:*`, flags only when they apply:

   ```bash
   gh issue create --repo IliaVoskoboinikov/Finance-Manager \
     --title "Починить балансы при смене счёта в редактировании операции" \
     --label bug --label area:data --body-file <scratch>/body.md
   ```

   The command prints the issue URL; its last segment is the number N.

4. Put it on the board with the priority (repeating the command is safe):

   ```bash
   python3 .github/scripts/board.py add N --priority P1
   ```

   The board also adds open issues by itself (Auto-add), but without a priority.

5. Report: `FM-N «title»` and the link.

Body sections (Russian, `### ` headings):

| Type | Sections |
| :--- | :--- |
| `bug` | Что происходит · Как воспроизвести · Ожидание · Где в коде · Источник |
| `enhancement`, `tech-debt`, `documentation`, epic | Зачем · Что сделать · Готово, когда · Затронутые модули · Вне задачи · Источник |

## Epic and sub-issues

An epic is an issue with the `epic` label; link each part as a sub-issue:

```bash
P=$(gh issue view <EPIC> --repo IliaVoskoboinikov/Finance-Manager --json id --jq .id)
C=$(gh issue view <PART> --repo IliaVoskoboinikov/Finance-Manager --json id --jq .id)
gh api graphql -f query='mutation($p:ID!,$c:ID!){addSubIssue(input:{issueId:$p,subIssueId:$c}){subIssue{number}}}' -f p="$P" -f c="$C"
```

## Take a task («возьми FM-N»)

1. Read it: `gh issue view N --repo IliaVoskoboinikov/Finance-Manager`. If it has
   `needs-decision`, stop and ask the user. If it is `blocked`, say what blocks it.
2. Check the digest; if two or more non-epic tasks are in progress, warn the user.
3. Move the card:

   ```bash
   python3 .github/scripts/board.py move "In Progress" --issue N --only-from Backlog
   ```

4. Propose the branch `feature/FM-N-<snake_case_name>`; git stays with the user.

## Other moves

```bash
python3 .github/scripts/board.py move "Backlog" --issue N        # work stopped before a PR
python3 .github/scripts/board.py priority N P2                   # only after the user agreed
```

In Review and Done are set by automation (PR opened, PR merged with `Closes #N`). Never move a
card to Done or close an issue yourself.

## At the end of a task

Report the key and title, the card status, the branch `feature/FM-N-…`, a commit message
ending with `(FM-N)`, and drafts of follow-up cards.
