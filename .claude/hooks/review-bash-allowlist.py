#!/usr/bin/env python3
"""PreToolUse hook (Bash) of the read-only review subagents: allow only reading the diff.

`fm-architecture-reviewer` and `fm-security-reviewer` review diffs that may come from a
public PR, so their input is untrusted. Their Bash is limited to what reading a diff
needs: `git diff/log/show/merge-base/status/rev-parse/ls-files/blame`, `gh pr diff/view`
and output filters after a pipe (`head`, `grep`, `sort`, … with known options and no file
operands), chained with pipes, `&&`, `;` or `$(…)`. Everything else is refused with exit
code 2, and so is any command that names a secret file, redirects output into a file or
passes `--output`. A separator inside quotes (`cut -d "|"`) splits the command too: such a
command is refused, never let through.

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
# Фильтр → (флаги без значения, флаги со значением). Других опций и файловых операндов нет:
# фильтр читает только stdin и ничего не пишет (`sort -o`, `uniq in out`, `grep -r` отпадают).
FILTERS = {
    "head": ("", "nc"),
    "tail": ("", "nc"),
    "wc": ("lwcm", ""),
    "sort": ("nruf", "kt"),
    "uniq": ("cdu", ""),
    "cut": ("", "dfc"),
    "grep": ("nivcEFwo", "emABC"),
}
SECRETS = re.compile(r"local\.properties|google-services\.json|\.jks\b|\.keystore\b")
# `2>&1` and `>/dev/null` only drop output; any other `>` writes a file.
HARMLESS_REDIRECTS = re.compile(r"\d?>&\d|\d?>\s*/dev/null")
SEGMENT_SPLIT = re.compile(r"(&&|\|\||\$\(|[;|\n()`])")


def filter_is_safe(program: str, args: list) -> bool:
    """The filter reads only stdin: known short options, no file operands.

    `grep` alone keeps one operand — the pattern — unless it is given with `-e`.
    """
    flags, with_value = FILTERS[program]
    operands, pattern_given, index = 0, False, 0
    while index < len(args):
        arg = args[index]
        index += 1
        if re.fullmatch(r"-\d+", arg) and program in ("head", "tail"):
            continue
        if re.fullmatch(r"-[A-Za-z]+\S*", arg):
            letter = arg[1]
            if letter in with_value:
                pattern_given |= letter == "e"
                if len(arg) == 2:
                    index += 1  # значение — следующий токен
                continue
            if all(c in flags for c in arg[1:]):
                continue
            return False
        if arg.startswith("-"):
            return False
        operands += 1
    return operands == 0 or (program == "grep" and operands == 1 and not pattern_given)


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
        # Фильтр читает только вывод предыдущей команды: первым в цепочке или с файловым
        # операндом он читал бы файлы (`grep -r TOKEN .` нашёл бы секреты в local.properties).
        if program in FILTERS:
            if index > 0 and parts[index - 1] == "|" and filter_is_safe(program, args):
                continue
            return f"runs `{program}` on files instead of the piped output"
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
