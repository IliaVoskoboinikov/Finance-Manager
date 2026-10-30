---
name: fm-task
description: Run a Finance-Manager board task end to end — read the card, interview the user, branch, plan, implement (bugs test-first), run the CI checks, loop the review and report with a commit message. Use when the user says "/fm-task FM-N", "возьми FM-N и сделай", "выполни задачу FM-N" or gives a card link and asks to do the task.
---

# Run a board task

One pipeline for a task from the board. Every step points at the rule that owns it; this
skill only fixes the order. The argument is `FM-N`, an issue number or a card link.

## 1. Take the card

Follow «Take a task» in the `board` skill: read the issue (stop on `needs-decision`, name
what blocks a `blocked` one), check the digest, move the card to In Progress. Its
«Готово, когда» section is the acceptance criteria for everything below.

Where the app can rename sessions, name this one `FM-N <snake_case_name>` — the branch name
without `feature/FM-N-`.

## 2. Interview

Study the code the card touches first, then interview as in `CLAUDE.md` («Clarify before
executing»): one question per card, recommended option first. Skip it when nothing that
changes the result is open, and say which assumptions you made. If the interview ends with
not doing the task, move the card back to Backlog («Other moves» in the `board` skill).

## 3. Branch

Branch `feature/FM-N-<snake_case_name>`. Create it only when the user asked to start on a
new branch, and only from a clean tree:

```bash
git fetch origin && git switch --no-track -c feature/FM-N-<snake_case_name> origin/master
```

A dirty tree or any other git step → ask; commits, pushes and PRs stay with the user.

## 4. Plan

A non-trivial task (several files or modules, a new pattern, DB / sync / navigation, a
client↔server contract) → plan mode: files, steps, risks and the checks you will run; no
code until the user approves the plan. A local, obvious change goes straight to step 5.

## 5. Implement

Mirror the surrounding code and the area rules in `docs/agents/*.md`. A bug fix starts with
a test that reproduces it (`docs/agents/testing.md`, «Bug fixes: test first»). New UI gets
`@Preview` and nav annotations; a changed module gets its `README.md` updated; non-trivial
design gets a doc in `docs/`.

## 6. Checks

Run every blocking CI job from `docs/agents/release-process.md` («Mandatory Verification»).
A docs-only change skips them.

## 7. Review loop

`docs/agents/release-process.md` («Automatic Review Loop»): `code-review` at `high` until no
valid finding or 3 rounds, then one final pass by the `pr-review-toolkit` agents.

## 8. Report

The completion report from `docs/agents/release-process.md`, then the ready git commands
for the user: `git add`, `git commit -m "<type>(<scope>): … (FM-N)"` (subject ≤ 72
characters), `git push -u origin feature/FM-N-…`, and the PR body line `Closes #N`. End by
asking the user to say when the PR is open — then «After the PR is opened» starts.
