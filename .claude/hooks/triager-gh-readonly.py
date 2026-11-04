#!/usr/bin/env python3
"""PreToolUse hook (Bash) of fm-coderabbit-triager: let it read GitHub, never write to it.

The triager reads CodeRabbit's comments — text from a public PR — so an injected comment
must not make it post, resolve or edit anything. Refused with exit code 2:

- `gh pr|issue comment/review/edit/close/…`, `gh auth token`, `curl` / `wget`, and shells
  or `eval` that would hide a command inside a string;
- `gh api` with a write method (`-X POST/PATCH/PUT/DELETE`) or with fields (`-f`, `-F`,
  `--input`), which turn a REST call into a POST;
- `gh api graphql` unless every `query=` is one single-quoted literal that starts with
  `query` or `{` and has no `mutation`: the shell can't build the text from pieces
  (`'mut''ation'`), substitutions (`$(…)`, backticks) or a file (`--input`, `=@file`).

Like deny-git.py it reads the command text — a guard, not a sandbox.
"""
import json
import re
import sys

GH_WRITE = re.compile(
    r"\bgh\s+(pr|issue)\s+(comment|review|edit|close|reopen|merge|create|ready|lock|delete)\b"
    r"|\bgh\s+auth\s+token\b|(^|[\s;&|(])(curl|wget|http|eval)\s|\b(bash|sh|zsh)\s+-\w*c"
)
WRITE_METHOD = re.compile(r"(-X|--method)[\s=]*(POST|PATCH|PUT|DELETE)", re.I)
FIELDS = re.compile(r"\s(-f|-F|--field|--raw-field|--input)[\s=]")
SEGMENT_SPLIT = re.compile(r"&&|\|\||\$\(|[;|\n`]")
QUERY_FIELD = re.compile(r"(?:-f|-F|--field|--raw-field)[\s=]+query=")
# Поле запроса целиком в одинарных кавычках: внутри них оболочка ничего не подставляет.
QUERY_LITERAL = re.compile(r"(?:-f|-F|--field|--raw-field)[\s=]+query='([^']*)'(?=\s|$)")
READ_QUERY = re.compile(r"\s*(query\b|\{)")


def graphql_refused(command: str) -> str:
    """Why a `gh api graphql` command is refused, or an empty string."""
    if re.search(r"\$\(|`|--input\b|=@", command):
        return "builds the GraphQL request from a substitution or a file"
    literals = QUERY_LITERAL.findall(command)
    if len(literals) != len(QUERY_FIELD.findall(command)) or not literals:
        return "passes a GraphQL query that is not one single-quoted literal"
    for literal in literals:
        if "mutation" in literal or not READ_QUERY.match(literal):
            return "sends a GraphQL mutation"
    return ""


def refused(command: str) -> str:
    """Why the command is refused, or an empty string when it only reads."""
    command = command.replace("\\\n", " ")
    if GH_WRITE.search(command):
        return "publishes or runs a command the hook can't read"
    if re.search(r"\bgh\s+api\s+graphql\b", command):
        reason = graphql_refused(command)
        if reason:
            return reason
    for part in SEGMENT_SPLIT.split(command):
        if not re.search(r"\bgh\s+api\b", part):
            continue
        if WRITE_METHOD.search(part):
            return "calls `gh api` with a write method"
        if not re.search(r"\bgh\s+api\s+graphql\b", part) and FIELDS.search(part):
            return "passes fields to a REST `gh api` call, which makes it a POST"
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
            "`gh api …` and `gh api graphql -f query='query …'`.",
            file=sys.stderr,
        )
        return 2
    return 0


if __name__ == "__main__":
    sys.exit(main())
