#!/usr/bin/env python3
"""PreToolUse hook (Bash): refuse the git and gh commands that stay with the user.

The `deny` rules in .claude/settings.json match the command text by prefix, so
`git -C . push`, `git -c k=v commit` or `bash -c "git push"` slip past them. This hook
parses the command instead: for every `git` / `gh` invocation it skips the global
options, takes the real subcommand and refuses it when it is commit / push / reset /
rebase or `gh pr create` / `gh pr merge`. `git stash push`, `git log --grep push` and
other commands are left alone — the subcommand there is `stash` / `log`.
"""
import json
import os
import shlex
import sys

GIT_DENIED = {"commit", "push", "reset", "rebase"}
GH_PR_DENIED = {"create", "merge"}
# Global options that take their value as the next token.
GIT_OPTS_WITH_ARG = {"-C", "-c", "--git-dir", "--work-tree", "--namespace", "--exec-path"}
GH_OPTS_WITH_ARG = {"-R", "--repo"}
SEPARATORS = set(";&|()\n")
SHELLS = {"bash", "sh", "zsh", "dash"}


def tokens(command: str) -> list:
    lexer = shlex.shlex(command, posix=True, punctuation_chars=";&|()")
    lexer.whitespace_split = True
    try:
        return list(lexer)
    except ValueError:  # unbalanced quotes: fall back to a plain split
        return command.split()


def subcommand(rest: list, opts_with_arg: set) -> tuple:
    """(subcommand, tokens after it) of one invocation, skipping its global options."""
    i = 0
    while i < len(rest):
        tok = rest[i]
        if set(tok) <= SEPARATORS:
            return None, []
        if tok.startswith("-"):
            i += 2 if tok in opts_with_arg else 1
            continue
        return tok, rest[i + 1:]
    return None, []


def runs_as_script(toks: list, i: int) -> bool:
    prev = toks[i - 1] if i >= 1 else ""
    if prev == "eval":
        return True
    # -c alone or in a cluster of short options (-c, -lc, -ec), the shell earlier in the same
    # command with any options between: bash -lc, bash -e -c, bash --norc -o pipefail -c.
    if not (prev.startswith("-") and not prev.startswith("--") and "c" in prev[1:]):
        return False
    for tok in reversed(toks[:i - 1]):
        if set(tok) <= SEPARATORS:
            return False
        if os.path.basename(tok) in SHELLS:
            return True
    return False


def denied(command: str, depth: int = 0) -> str:
    toks = tokens(command)
    for i, tok in enumerate(toks):
        name = os.path.basename(tok)
        if name == "git":
            sub, _ = subcommand(toks[i + 1:], GIT_OPTS_WITH_ARG)
            if sub in GIT_DENIED:
                return f"git {sub}"
        elif name == "gh":
            sub, after = subcommand(toks[i + 1:], GH_OPTS_WITH_ARG)
            if sub == "pr":
                action, _ = subcommand(after, GH_OPTS_WITH_ARG)
                if action in GH_PR_DENIED:
                    return f"gh pr {action}"
        elif depth < 2 and runs_as_script(toks, i):
            # A quoted script that is executed: bash -c "git push", eval '...'.
            # Quoted text that is only data (echo "git push", grep 'git push') is left alone.
            found = denied(tok, depth + 1)
            if found:
                return found
    return ""


def main() -> int:
    try:
        data = json.load(sys.stdin)
    except Exception:
        return 0
    command = (data.get("tool_input") or {}).get("command") or ""
    found = denied(command)
    if found:
        print(json.dumps({
            "hookSpecificOutput": {
                "hookEventName": "PreToolUse",
                "permissionDecision": "deny",
                "permissionDecisionReason": (
                    f"`{found}` stays with the user (CLAUDE.md, «Never run git yourself»): "
                    "leave the change in the working tree and give the user the command."
                ),
            }
        }))
    return 0


if __name__ == "__main__":
    sys.exit(main())
