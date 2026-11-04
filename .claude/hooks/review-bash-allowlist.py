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
import json
import re
import sys

from agent_shell import GIT_READ, filter_is_safe, git_read_refused, split_command

GH_PR_READ = {"diff", "view"}
# Фильтр → (флаги без значения, флаги со значением, сколько операндов можно). Файловых
# операндов нет: фильтр читает только stdin и ничего не пишет (`sort -o`, `uniq in out`,
# `grep -r` отпадают); у grep единственный операнд — шаблон.
FILTERS = {
    "head": ("", "nc", 0),
    "tail": ("", "nc", 0),
    "wc": ("lwcm", "", 0),
    "sort": ("nruf", "kt", 0),
    "uniq": ("cdu", "", 0),
    "cut": ("", "dfc", 0),
    "grep": ("nivcEFwo", "emABC", 1),
}
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
        return ""
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


def main() -> int:
    try:
        command = json.load(sys.stdin).get("tool_input", {}).get("command", "")
    except Exception:
        print("review-bash-allowlist: unreadable hook input", file=sys.stderr)
        return 2
    reason = refused(command)
    if reason:
        print(
            f"Read-only reviewer: the command {reason}. Allowed: git "
            f"{'/'.join(sorted(GIT_READ))}, gh pr diff/view, "
            f"{', '.join(sorted(FILTERS))} after a pipe.",
            file=sys.stderr,
        )
        return 2
    return 0


if __name__ == "__main__":
    sys.exit(main())
