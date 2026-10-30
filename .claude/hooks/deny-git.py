#!/usr/bin/env python3
"""PreToolUse hook (Bash): refuse the git and gh commands that stay with the user.

The `deny` rules in .claude/settings.json match the command text by prefix, so
`git -C . push`, `git -c k=v commit` or `bash -c "git push"` slip past them. This hook
parses the command instead: for every `git` / `gh` invocation it skips the global
options, takes the real subcommand and refuses it when it is commit / push / reset /
rebase or `gh pr create` / `gh pr merge`. `git stash push`, `git log --grep push` and
other commands are left alone — the subcommand there is `stash` / `log`.

It covers the forms an agent writes, nested `sh -c` / `eval` included; it is not a security
boundary. A command built at run time (`$(echo git) push`, a variable, a git alias, a script
file) is not read — the rule in CLAUDE.md («Never run git yourself») still binds the agent,
and only a sandbox enforces anything regardless of the command text.
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
# Shell options that take their value as the next token (bash -o pipefail, -O extglob).
SHELL_OPTS_WITH_ARG = {"-o", "+o", "-O", "+O"}


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
    # -c alone or in a cluster of short options (-c, -lc, -ec), preceded only by the shell's own
    # options: bash -lc, bash -e -c, bash --norc -o pipefail -c. A -c after the script is an
    # argument of that script (bash -c 'rg "$@"' _ -c 'git push'), not a new one.
    if not (prev.startswith("-") and not prev.startswith("--") and "c" in prev[1:]):
        return False
    j = i - 2
    while j >= 0:
        tok = toks[j]
        if os.path.basename(tok) in SHELLS:
            return True
        is_option = tok.startswith(("-", "+"))
        is_option_value = j >= 1 and toks[j - 1] in SHELL_OPTS_WITH_ARG
        if not (is_option or is_option_value):
            return False
        j -= 1
    return False


def denied(command: str) -> str:
    # A worklist instead of recursion with a depth cap: any nesting of bash -c is unwrapped.
    # It ends because every nested script is a strict part of the command around it.
    pending = [command]
    while pending:
        toks = tokens(pending.pop())
        for i, tok in enumerate(toks):
            # Script or program is told by position, not by spaces: after -c / eval the token
            # is a script (git push origin feature/git), elsewhere a program path may hold
            # spaces ("/opt/my tools/git" push).
            script = runs_as_script(toks, i)
            name = "" if script else os.path.basename(tok)
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
            if script:
                # A quoted script that is executed: bash -c "git push", eval '...'.
                # Quoted text that is only data (echo "git push", grep 'git push') is left alone.
                if toks[i - 1] == "eval":
                    # eval joins its unquoted arguments with spaces and parses the result
                    # again, as bash does: eval git push, eval 'git commit -m x'.
                    args = []
                    for arg in toks[i:]:
                        if set(arg) <= SEPARATORS:
                            break
                        args.append(arg)
                    pending.append(" ".join(args))
                else:
                    pending.append(tok)
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
