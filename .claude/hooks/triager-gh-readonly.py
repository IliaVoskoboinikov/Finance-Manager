#!/usr/bin/env python3
"""PreToolUse hook (Bash) of fm-coderabbit-triager: let it read GitHub, never write to it.

The triager reads CodeRabbit's comments — text from a public PR — so an injected comment
must not make it post, resolve or edit anything. The command is cut into simple commands
the way the shell runs them (`agent_shell.split_command`: quotes removed, a single `&`
separates), and each one must be:

- `gh api` reading: no write method, no `--input`; a REST call takes no fields (a field
  makes it a POST); `gh api graphql` takes exactly one `query` — a `query …` or `{ … }`
  document without `mutation` — and plain variables;
- `gh pr view/diff/list/checks`, `gh issue view/list`, `gh repo view`;
- `git` reading (as in review-bash-allowlist.py) or a filter after a pipe.

Everything else (`curl`, `gh auth token`, `eval`, `sh -c`, files with `@`) is refused with
exit code 2. Like deny-git.py it reads the command — a guard, not a sandbox.
"""
import json
import re
import sys

from agent_shell import filter_is_safe, split_command

GH_READ = {"pr": {"view", "diff", "list", "checks"}, "issue": {"view", "list"}, "repo": {"view"}}
GIT_READ = {"diff", "log", "show", "merge-base", "status", "rev-parse", "ls-files", "blame"}
FILTERS = {
    "head": ("", "nc", 0),
    "tail": ("", "nc", 0),
    "wc": ("lwcm", "", 0),
    "sort": ("nruf", "kt", 0),
    "uniq": ("cdu", "", 0),
    "grep": ("nivcEFwo", "emABC", 1),
    "jq": ("rcsje", "", 1),
}
# Флаги gh api: булевы и те, что берут значение (короткие можно склеивать: `-ifbody=x`).
SHORT_BOOL = set("i")
SHORT_VALUE = set("HXqtfFp")
LONG_BOOL = {"--paginate", "--slurp", "--include", "--silent", "--verbose"}
LONG_VALUE = {"--method", "--jq", "--template", "--header", "--hostname", "--cache",
              "--preview", "--field", "--raw-field"}
FIELD_FLAGS = {"f", "F", "--field", "--raw-field"}
READ_QUERY = re.compile(r"\s*(query\b|\{)")
VARIABLE = re.compile(r"[A-Za-z_]\w*=[\w.:/-]*")


def parse_api(args: list):
    """Split `gh api` arguments into (endpoint, method, fields) or raise ValueError."""
    endpoint, method, fields, index = None, "GET", [], 0

    def take(flag: str, value: str) -> None:
        nonlocal method
        if flag in ("X", "--method"):
            method = value.upper()
        elif flag in FIELD_FLAGS:
            fields.append((flag, value))

    while index < len(args):
        arg = args[index]
        index += 1
        if arg == "--input" or arg.startswith("--input="):
            raise ValueError("sends a request body from a file")
        if arg.startswith("--"):
            name, eq, value = arg.partition("=")
            if name in LONG_BOOL and not eq:
                continue
            if name not in LONG_VALUE:
                raise ValueError(f"passes an unknown `gh api` option {name}")
            if not eq:
                if index >= len(args):
                    raise ValueError(f"leaves {name} without a value")
                value, index = args[index], index + 1
            take(name, value)
        elif arg.startswith("-") and len(arg) > 1:
            for position, char in enumerate(arg[1:], start=1):
                if char in SHORT_BOOL:
                    continue
                if char not in SHORT_VALUE:
                    raise ValueError(f"passes an unknown `gh api` option -{char}")
                value = arg[position + 1:]
                if not value:
                    if index >= len(args):
                        raise ValueError(f"leaves -{char} without a value")
                    value, index = args[index], index + 1
                take(char, value)
                break
        elif endpoint is None:
            endpoint = arg
        else:
            raise ValueError("passes an extra positional argument to `gh api`")
    return endpoint, method, fields


def api_refused(args: list) -> str:
    try:
        endpoint, method, fields = parse_api(args)
    except ValueError as error:
        return str(error)
    if method != "GET":
        return f"calls `gh api` with {method}"
    if endpoint != "graphql":
        return "passes fields to a REST `gh api` call, which makes it a POST" if fields else ""
    queries = [value for _, value in fields if value.startswith("query=")]
    if len(queries) != 1:
        return "passes no or several GraphQL `query` fields"
    query = queries[0][len("query="):]
    if "mutation" in query or not READ_QUERY.match(query):
        return "sends a GraphQL mutation"
    for flag, value in fields:
        if value.startswith("query="):
            continue
        if value.startswith("operationName=") or not VARIABLE.fullmatch(value):
            return "passes a GraphQL field other than a plain variable"
    return ""


def segment_refused(separator: str, tokens: list) -> str:
    """Why one simple command is refused, or an empty string."""
    program, args = tokens[0], tokens[1:]
    if program in FILTERS:
        if separator == "|" and filter_is_safe(args, *FILTERS[program]):
            return ""
        return f"runs `{program}` on files instead of the piped output"
    if program == "git":
        args = [a for a in args if a != "--no-pager"]
        if args[:1] and args[0] in GIT_READ and not any(
            a.startswith("--output") or a == "--no-index" for a in args
        ):
            return ""
        return "runs a git command that is not read-only"
    if program == "gh" and args:
        if args[0] == "api":
            return api_refused(args[1:])
        if args[1:2] and args[1] in GH_READ.get(args[0], set()):
            return ""
        return f"runs `gh {' '.join(args[:2])}`, which is not read-only"
    return f"runs `{program}`, which is not on the read-only list"


def refused(command: str) -> str:
    """Why the command is refused, or an empty string when it only reads."""
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
        print("triager-gh-readonly: unreadable hook input", file=sys.stderr)
        return 2
    reason = refused(command)
    if reason:
        print(
            f"fm-coderabbit-triager is read-only: the command {reason}. Read with GET "
            "`gh api …`, `gh api graphql -f query='query …'`, `gh pr view`.",
            file=sys.stderr,
        )
        return 2
    return 0


if __name__ == "__main__":
    sys.exit(main())
