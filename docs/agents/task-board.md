# Task Board

Tasks live on the [GitHub Projects board](https://github.com/users/IliaVoskoboinikov/projects/5).
The process (statuses, priorities, labels, automation) is described in
[`docs/task-tracking.md`](../task-tracking.md); this file is the agent's part of it. Board
commands are in [`.github/scripts/board.py`](../../.github/scripts/board.py).

## Key, branch, commit
*   A task is an issue; its key is `FM-<issue number>`: `FM-42` is issue #42.
*   Branch: `feature/FM-<number>-<snake_case_name>`, e.g. `feature/FM-42-fix_navigation`.
    Task branches live under `feature/` like all others; the automation finds the card by the
    `FM-N` part.
*   Commit subject: Conventional Commits in Russian, key at the end, at most 72 characters —
    GitHub cuts a longer subject when it becomes a PR title:
    `fix(navigation): починить возврат с экрана счёта (FM-42)`.
*   PR body: `Closes #42` (the automation adds it if it is missing).
*   Git stays with the user: propose the branch name and the commit message, don't create them.

## When to propose a card
*   The user asks for a task.
*   You find a bug or tech debt outside the current task: don't fix it silently and don't
    widen the scope — propose a card.
*   Work is deliberately deferred (a follow-up, a known limitation to fix later): propose a card
    instead of a `TODO` comment or a checklist in `docs/`.
*   Docs describe how things work and why; task lists and checklists of open work do not go
    into `docs/` — they go to the board.

## Creating a card
1.  Look for duplicates first: `gh issue list --state all --search "<keywords>"`.
2.  Show a draft in chat: title, labels, priority, body. Create it only after the user's
    explicit "yes" — the repository is public. One "yes" covers the cards it was given for.
3.  Create the issue and put it on the board with its priority (see the `board` skill for
    exact commands). Report the link and the key.

**Title:** an imperative in Russian, without a prefix — «Починить балансы при смене счёта».
The body is in Russian and uses the same sections as the issue forms:

| Type | Sections (`### …`) |
| :--- | :--- |
| Bug | Что происходит · Как воспроизвести · Ожидание · Где в коде · Что нужно от бэкенда · Источник |
| Feature, tech debt, epic | Зачем · Что сделать · Готово, когда · Затронутые модули · Вне задачи · Что нужно от бэкенда · Источник |

«Источник» says where the task came from: «найдено при работе над FM-57», «аудит C3».

**Backend work never gets its own card.** If a task needs server changes, put the
`needs-backend` label on that task and add the «Что нужно от бэкенда» section: endpoints,
fields, response codes and server behaviour the task depends on, detailed enough to build the
server part from that section alone. The label and the section always go together; tasks
without server work have no such section.

**Labels:** exactly one type (`bug`, `enhancement`, `tech-debt`, `documentation`); one or more
`area:*`; flags only when they apply (`blocked`, `needs-backend`, `needs-decision`); `epic`
for a multi-step result, with its parts linked as sub-issues. Never set `ci-failure` or
`dependencies` — they belong to automation.

**Priority:** P0 — broken now (data or money corrupted, security hole, red `master`);
P1 — needed before release v1.0 or a noticeable bug; P2 — improvement, tech debt, minor bug;
P3 — someday. If unsure, propose one and say why; the user decides.

## Moving cards
*   **Starting a task** — move its card to In Progress yourself, without asking. Before that,
    check the digest: if two or more non-epic tasks are already in progress, warn the user.
*   Everything else is automated: In Review when the PR is opened, back to In Progress for a
    draft or a PR closed without merge, Done when the PR is merged with `Closes #N`.
*   If you stop working on a task before a PR exists, move it back to Backlog and say so.
*   Never move a card to Done, never close or delete issues.
*   Don't start `needs-decision` tasks — they wait for the owner's decision.
*   Changing an existing card's title, body, labels or priority — ask first.

## Reporting
At the end of a task add to the completion report: the key and title (`FM-42 …`), the card's
status, the proposed branch name, the commit message with `(FM-N)`, and drafts of follow-up
cards if any.
