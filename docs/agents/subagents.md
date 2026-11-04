# Project Subagents

Claude Code subagents of this project live in `.claude/agents/`. They take the work that
floods the main session's context (Gradle logs, CodeRabbit threads) or needs a narrow,
project-specific reviewer. Every one of them only reports: the main session triages, fixes,
talks to the user and runs git, the board and the report.

## The agents

| Agent | Called by the main session | Input | Returns |
| :--- | :--- | :--- | :--- |
| `fm-ci-runner` | «Mandatory Verification» in `release-process.md`: before the report and after a fix that touches code | `all` (default) or jobs / modules | pass/fail table, the first error of each failure with `file:line`, the log directory |
| `fm-architecture-reviewer` | review loop, round 1, in parallel with `code-review` | the diff (default: working tree vs `origin/master`) | findings on project invariants CI can't check, 🔴/🟠/🟡 with the rule and a scenario |
| `fm-security-reviewer` | review loop, round 1, only when the diff touches the paths in its description | same | security findings: keys, PIN, biometrics, secrets and PII in logs, backups |
| `fm-coderabbit-triager` | «After the PR is opened»: each time CodeRabbit has reviewed a new head | PR number | the review state for the head and a verdict per comment: valid / reject with a reason / card |

None of them edits files, rewrites baselines, publishes to GitHub, moves cards or spawns
agents. Their verdicts are proposals: check a finding against the code before fixing it, and
a reject reason before it goes to a PR thread (that still needs the user's yes).

## How they are built

*   **Rules are not copied into prompts.** A custom subagent loads `CLAUDE.md` with every
    `@docs/agents/*.md` it imports, so prompts point at the rule (file and section) instead of
    restating it. `fm-ci-runner` needs none of it and sets `omitClaudeMd: true`; it reads the
    job list from `release-process.md` itself. This file is linked, not imported, so it
    doesn't reach every subagent's context.
*   **The workflow in `CLAUDE.md` is not theirs.** They see the interview, review-loop and
    report rules too, so each prompt starts by saying those belong to the main session.
*   **Read-only through `tools`.** `permissionMode` in the frontmatter is overridden by the main
    session's mode (auto, acceptEdits), so the allowlist is the only real limit: no `Edit` /
    `Write`, no `Agent` (subagents can nest up to three levels otherwise). `AskUserQuestion` is
    never available to a subagent. An agent that needs `Bash` for `gh` gets a frontmatter
    `PreToolUse` hook instead: `fm-coderabbit-triager` blocks `gh pr|issue comment/review/…`,
    GraphQL `mutation` and `gh api` writes (`-X POST…`, `-f` outside `graphql`) — the
    repository is public, so its read-only rule is enforced, not only asked for.
*   **Hooks.** The `PreToolUse` hook from `.claude/settings.json` (`deny-git.py`) runs for
    subagents too. The `Stop` hook (`verify-changes.py`: ktlint and detekt) does not — a
    subagent ends with `SubagentStop`. An agent that writes Kotlin would have to run
    `ktlintFormat` / `ktlintCheck` / `detekt` itself.
*   **Report format** of the reviewers follows `.github/copilot-instructions.md` (severity, what
    is wrong → scenario → fix) and takes its «already checked by CI» and «deliberate
    decisions» lists as out of scope.

## Gotchas

*   Claude Code watches `.claude/agents/` only if the directory existed when the session
    started: after the first agent file in a new directory, restart the session. Edits to an
    existing agent are picked up within seconds.
*   An agent file that exists only in a git worktree (not yet in the main checkout) was not
    found even by a desktop-app session started later in that worktree (FM-297, «Agent type
    not found»). Check a new or changed agent from a session that lists it — the Agent tool's
    list of types shows what was loaded — before relying on it.
*   A subagent spawned from the main session runs in the background by default; its result
    arrives as a notification in a later turn. Don't end the turn on «waiting» when the next
    step needs the result — do work that doesn't, or say what you are waiting for.
*   A subagent starts in the main session's working directory, so in a git worktree it builds
    and reviews the worktree.

## Adding or changing an agent

*   One job per agent, a description that says when to call it (the main session delegates by
    it), the narrowest `tools`, a `model` that fits the job (`haiku` for mechanical runs).
*   Prompts in English, reports in Russian, like the rest of the agent instructions.
*   Update the table above and the place in the process that calls the agent
    (`release-process.md`, the `fm-task` skill).
