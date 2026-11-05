#!/usr/bin/env python3
"""PreToolUse hook (Bash) of the read-only review subagents: allow only reading the diff.

`fm-architecture-reviewer` and `fm-security-reviewer` review diffs that may come from a
public PR, so their input is untrusted. Their Bash is limited to what reading a diff
needs. The command is cut into simple commands the way the shell runs them
(`agent_shell.split_command`), and each one must be:

- `git diff/log/show/merge-base/status/rev-parse/ls-files/blame` (`--no-pager` allowed),
  without `--output`, `--no-index` and `--contents` (the ways these commands write a file or
  read one outside git) — `agent_shell.git_read_refused`;
- `gh pr diff` / `gh pr view`;
- a filter after a pipe (`head`, `grep`, `sort`, …) with known options and no file operands.

Everything else is refused with exit code 2, and so is any command that names a secret
file. The hook is wired in the agents' frontmatter, not in .claude/settings.json: the main
session's Bash stays as it is. It reads the command, like deny-git.py — a guard against
injected or careless commands, not a sandbox.
"""
import re
import sys

from agent_shell import (
    GIT_READ, READ_FILTERS, filter_is_safe, git_read_refused, jq_refused, run_hook, split_command,
)

GH_PR_READ = {"diff", "view"}
FILTERS = {**READ_FILTERS, "cut": ("", "dfc", 0)}
SECRETS = re.compile(r"local\.properties|google-services\.json|\.jks\b|\.keystore\b")


def segment_refused(separator: str, tokens: list) -> str:
    """Why one simple command is refused, or an empty string."""
    program, args = tokens[0], tokens[1:]
    if program in FILTERS:
        # Первым в цепочке фильтр читал бы файлы (`grep -r TOKEN .` нашёл бы секреты).
        if separator == "|" and filter_is_safe(args, *FILTERS[program]):
            return ""
        return f"runs `{program}` on files instead of the piped output"
    if program == "git":
        return git_read_refused(args)
    if program == "gh" and args[:1] == ["pr"] and args[1:2] and args[1] in GH_PR_READ:
        return jq_refused(args)
    return f"runs `{program}`, which is not on the read-only list"


def refused(command: str) -> str:
    """Why the command is refused, or an empty string when it is allowed."""
    if SECRETS.search(command):
        return "names a secret file"
    segments, reason = split_command(command)
    if reason:
        return reason
    for separator, tokens in segments:
        reason = segment_refused(separator, tokens)
        if reason:
            return reason
    return ""


if __name__ == "__main__":
    sys.exit(run_hook(
        "Read-only reviewer",
        refused,
        f"Allowed: git {'/'.join(sorted(GIT_READ))}, gh pr diff/view, "
        f"{', '.join(sorted(FILTERS))} after a pipe.",
    ))
