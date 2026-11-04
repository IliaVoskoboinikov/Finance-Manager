"""Shell command parsing shared by the subagents' allowlist hooks.

The hooks of the read-only subagents (`review-bash-allowlist.py`,
`triager-gh-readonly.py`) must judge the command the shell will run, not its text: quotes
(`"-f"body=x`, `'mut''ation'`) and a single `&` hide a command from a regular expression.
`split_command` tokenizes like the shell does (`shlex`, operators as separate tokens, quotes
removed) and cuts the command into simple commands; a hook then allows each one by its
program and arguments, and refuses everything it does not know.

What the shell would expand at run time and `shlex` can't show — backticks, `${…}`,
`$'…'`, `$"…"`, `$(…)` inside quotes, `<(…)`, an assignment of a literal — is refused here.
An unquoted `$(…)` is allowed: its inner command becomes a segment of its own and is
checked like the rest. A separator inside quotes (`cut -d "|"`) still splits the command;
that only makes a hook refuse more, never less.
"""
import re
import shlex
from typing import List, Optional, Tuple

SEPARATORS = {"&&", "||", "|", ";", "&", "(", ")"}
# Подстановки, которые shlex не раскрывает и не показывает: их значение известно только оболочке.
RAW_EXPANSIONS = re.compile(r"`|\$\{|\$'|\$\"")
ASSIGNMENT = re.compile(r"[A-Za-z_]\w*=")

Segment = Tuple[str, List[str]]  # (разделитель перед сегментом, токены сегмента)


PUNCTUATION_ONLY = re.compile(r"[();<>|&]+")
OPERATOR = re.compile(r"&&|\|\||>>|>&|&>|<\(|[;&|()<>]")


def _tokens(command: str) -> List[str]:
    lexer = shlex.shlex(command, posix=True, punctuation_chars=True)
    lexer.whitespace_split = True
    tokens: List[str] = []
    for token in lexer:
        # shlex склеивает соседние операторы в один токен (`);`) — режем их обратно
        if PUNCTUATION_ONLY.fullmatch(token):
            tokens.extend(OPERATOR.findall(token))
        else:
            tokens.append(token)
    return tokens


def _drop_harmless_redirects(tokens: List[str]) -> Optional[List[str]]:
    """Remove `2>&1` and `>/dev/null`; None when any other redirect is left."""
    result: List[str] = []
    index = 0
    while index < len(tokens):
        token = tokens[index]
        if token == ">&" and tokens[index + 1:index + 2] == ["1"] and result[-1:] == ["2"]:
            result.pop()
            index += 2
            continue
        if token == ">" and tokens[index + 1:index + 2] == ["/dev/null"]:
            if result[-1:] in (["1"], ["2"]):
                result.pop()
            index += 2
            continue
        if any(c in token for c in "<>") and not any(c.isalnum() for c in token):
            return None
        result.append(token)
        index += 1
    return result


def split_command(command: str) -> Tuple[List[Segment], str]:
    """Cut a Bash command into simple commands.

    Returns the segments and an empty string, or no segments and the reason the command
    can't be judged.
    """
    command = command.replace("\\\n", " ").replace("\n", ";")
    if RAW_EXPANSIONS.search(command):
        return [], "uses an expansion only the shell can resolve (`…`, ${…}, $'…')"
    try:
        tokens = _tokens(command)
    except ValueError:
        return [], "cannot be parsed"
    tokens = _drop_harmless_redirects(tokens)
    if tokens is None:
        return [], "redirects input or output"
    segments: List[Segment] = []
    separator, current = "", []
    for index, token in enumerate(tokens):
        next_token = tokens[index + 1] if index + 1 < len(tokens) else ""
        if token in SEPARATORS:
            segments.append((separator, current))
            separator, current = token, []
            continue
        # `$(` без кавычек приходит как `$` (или `name=$`) перед `(`: сама подстановка
        # станет отдельным сегментом, а от слова ничего не остаётся.
        if next_token == "(" and (token == "$" or re.fullmatch(r"[A-Za-z_]\w*=\$", token)):
            continue
        if "$(" in token:
            return [], "uses a command substitution inside quotes"
        current.append(token)
    segments.append((separator, current))
    segments = [(sep, toks) for sep, toks in segments if toks]
    for _, toks in segments:
        if ASSIGNMENT.match(toks[0]):
            return [], "sets a shell variable"
    return segments, ""


def filter_is_safe(args: List[str], flags: str, with_value: str, operands_allowed: int) -> bool:
    """A filter that reads only stdin: known short options, at most `operands_allowed` operands.

    `flags` take no value, `with_value` take one (`-n 5` or `-n5`); `-<digits>` counts as
    an option for `head` / `tail` style filters when `with_value` holds `n`.
    """
    operands, index = 0, 0
    while index < len(args):
        arg = args[index]
        index += 1
        if re.fullmatch(r"-\d+", arg) and "n" in with_value:
            continue
        if re.fullmatch(r"-[A-Za-z]\S*", arg):
            letter = arg[1]
            if letter in with_value:
                if letter == "e":
                    operands_allowed = 0  # шаблон grep уже задан через -e
                if len(arg) == 2:
                    index += 1  # значение — следующий токен
                continue
            if all(c in flags for c in arg[1:]):
                continue
            return False
        if arg.startswith("-"):
            return False
        operands += 1
    return operands <= operands_allowed
