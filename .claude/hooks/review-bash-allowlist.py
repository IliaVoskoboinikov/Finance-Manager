#!/usr/bin/env python3
"""PreToolUse hook (Bash) of the read-only review subagents: allow only reading the diff.

`fm-architecture-reviewer` and `fm-security-reviewer` review diffs that may come from a
public PR, so their input is untrusted. Their Bash is limited to what reading a diff
needs: `git diff/log/show/merge-base/status/rev-parse/ls-files/blame`, `gh pr diff/view`
and output filters, chained with pipes, `&&`, `;` or `$(…)`. Everything else is refused
with exit code 2, and so is any command that names a secret file, redirects output into a
file or passes `--output`.

The hook is wired in the agents' frontmatter, not in .claude/settings.json: the main
session's Bash stays as it is. Like deny-git.py it reads the command text — a guard
against injected or careless commands, not a sandbox.
"""
import json
import re
import shlex
import sys

GIT_READ = {"diff", "log", "show", "merge-base", "status", "rev-parse", "ls-files", "blame"}
GH_PR_READ = {"diff", "view"}
FILTERS = {"head", "tail", "wc", "sort", "uniq", "grep", "cut", "nl"}
SECRETS = re.compile(r"local\.properties|google-services\.json|\.jks\b|\.keystore\b")
# `2>&1` and `>/dev/null` only drop output; any other `>` writes a file.
HARMLESS_REDIRECTS = re.compile(r"\d?>&\d|\d?>\s*/dev/null")
SEGMENT_SPLIT = re.compile(r"(&&|\|\||\$\(|[;|\n()`])")


def refused(command: str) -> str:
    """Return why the command is refused, or an empty string when it is allowed."""
    if SECRETS.search(command):
        return "names a secret file"
    if ">" in HARMLESS_REDIRECTS.sub("", command):
        return "redirects output into a file"
    parts = SEGMENT_SPLIT.split(command.replace("\\\n", " "))
    # re.split с группой чередует сегменты и разделители: [сегмент, разделитель, сегмент, …]
    for index in range(0, len(parts), 2):
        try:
            tokens = shlex.split(parts[index])
        except ValueError:
            return "cannot be parsed"
        if not tokens:
            continue
        if any(token.startswith("--output") for token in tokens):
            return "writes a file (--output)"
        program, args = tokens[0], [t for t in tokens[1:] if t != "--no-pager"]
        # Фильтр читает только вывод предыдущей команды: первым в цепочке он читал бы файлы
        # (`grep -r TOKEN .` нашёл бы секреты в local.properties).
        if program in FILTERS and index > 0 and parts[index - 1] == "|":
            continue
        if program == "git" and args and args[0] in GIT_READ:
            continue
        if program == "gh" and args[:1] == ["pr"] and args[1:2] and args[1] in GH_PR_READ:
            continue
        return f"runs `{program}`, which is not on the read-only list"
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
            f"{'/'.join(sorted(GIT_READ))}, gh pr diff/view, {', '.join(sorted(FILTERS))}.",
            file=sys.stderr,
        )
        return 2
    return 0


if __name__ == "__main__":
    sys.exit(main())
