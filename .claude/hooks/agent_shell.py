"""Shell command parsing shared by the subagents' allowlist hooks.

The hooks of the read-only subagents (`review-bash-allowlist.py`,
`triager-gh-readonly.py`) must judge the command the shell will run, not its text: quotes
(`"-f"body=x`, `'mut''ation'`) and a single `&` hide a command from a regular expression.
`split_command` tokenizes like the shell does (`shlex`, operators as separate tokens, quotes
removed) and cuts the command into simple commands; a hook then allows each one by its
program and arguments, and refuses everything it does not know.

What the shell would expand at run time and `shlex` can't show — backticks, `${…}`,
`$'…'`, `$"…"`, `$(…)` inside quotes, `<(…)`, `{a,b}` and globs (`*`, `?`, `[…]`)
outside quotes, an assignment of a literal, `$name` outside quotes — is refused here. `#` is not a comment for `shlex` (inside
a word it isn't one for the shell either), and `\⏎` joins words as the shell does.
An unquoted `$(…)` is allowed: its inner command becomes a segment of its own and is
checked like the rest. Used directly as arguments, its output would be split into words, so
there it may only be `git merge-base` / `git rev-parse` (a hash); in `name=$(…)` it may be
any allowed read. A separator inside quotes (`cut -d "|"`) still splits the command;
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


GIT_READ = {"diff", "log", "show", "merge-base", "status", "rev-parse", "ls-files", "blame"}
# Опции, которыми эти команды пишут файл или читают файл вне git (`blame --contents`).
GIT_FILE_OPTIONS = ("--output", "--no-index", "--contents")
BRACE_EXPANSION = re.compile(r"\{[^{}\s]*(,|\.\.)[^{}\s]*\}")


def _unquoted(command: str) -> str:
    """The command with the content of quotes replaced by `Q`: what the shell expands."""
    result, quote, index = [], "", 0
    while index < len(command):
        char = command[index]
        if quote:
            if char == quote:
                quote = ""
            elif char == "\\" and quote == '"':
                index += 1
            result.append("Q")
        elif char in "'\"":
            quote = char
            result.append("Q")
        elif char == "\\":
            index += 1
            result.append("Q")
        else:
            result.append(char)
        index += 1
    return "".join(result)


def git_read_refused(args: List[str]) -> str:
    """Why `git <args>` is not a plain read, or an empty string (`--no-pager` is dropped)."""
    args = [a for a in args if a != "--no-pager"]
    if not args or args[0] not in GIT_READ:
        return "runs a git command that is not read-only"
    if any(option in arg for arg in args for option in GIT_FILE_OPTIONS):
        return "makes git write a file or read one outside the repository"
    return ""


def _tokens(command: str) -> List[str]:
    lexer = shlex.shlex(command, posix=True, punctuation_chars=True)
    lexer.whitespace_split = True
    # Для оболочки `#` внутри слова — просто символ, а shlex отрезал бы им остаток команды.
    lexer.commenters = ""
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
    # Оболочка удаляет `\⏎` без пробела: `--out\⏎put` — это одно слово `--output`.
    command = command.replace("\\\n", "").replace("\n", ";")
    if RAW_EXPANSIONS.search(command):
        return [], "uses an expansion only the shell can resolve (`…`, ${…}, $'…')"
    unquoted = _unquoted(command)
    # `{a,b}` вне кавычек оболочка размножит в несколько слов, а после shlex этого не видно.
    if BRACE_EXPANSION.search(unquoted):
        return [], "uses a brace expansion"
    # `*`, `?`, `[…]` вне кавычек оболочка заменит именами файлов — в том числе файла
    # `--output=x`, который мог принести сам проверяемый PR.
    if re.search(r"[*?\[]", unquoted):
        return [], "uses an unquoted glob (quote the pattern)"
    # `$name` вне кавычек оболочка разобьёт на слова: значение из файла PR стало бы флагами.
    if re.search(r"\$[^(\s]", unquoted):
        return [], "expands a variable outside quotes (quote it)"
    try:
        tokens = _tokens(command)
    except ValueError:
        return [], "cannot be parsed"
    tokens = _drop_harmless_redirects(tokens)
    if tokens is None:
        return [], "redirects input or output"
    segments: List[Segment] = []
    # Внешние команды, прерванные подстановкой `$(…)`: после её `)` разбор продолжает их.
    outer: List[Segment] = []
    separator, current = "", []
    index = 0
    while index < len(tokens):
        token = tokens[index]
        next_token = tokens[index + 1] if index + 1 < len(tokens) else ""
        index += 1
        # `$(` без кавычек приходит как слово, оканчивающееся на `$`, перед `(`. Подстановка
        # станет отдельным сегментом; прямо в аргументах (не в `name=$(…)`) её вывод оболочка
        # разобьёт на слова, поэтому такой сегмент помечается разделителем `$(`.
        if next_token == "(" and token.endswith("$"):
            assignment = re.fullmatch(r"[A-Za-z_]\w*=\$", token)
            if not assignment:
                current.append(token[:-1] + "SUBSTITUTION")
            outer.append((separator, current))
            separator, current = ("(" if assignment else "$("), []
            index += 1  # пропускаем `(`
            continue
        if token == ")" and outer:
            segments.append((separator, current))
            separator, current = outer.pop()
            continue
        if token in SEPARATORS:
            segments.append((separator, current))
            separator, current = token, []
            continue
        if "$(" in token:
            return [], "uses a command substitution inside quotes"
        current.append(token)
    if outer:
        return [], "cannot be parsed"
    segments.append((separator, current))
    segments = [(sep, toks) for sep, toks in segments if toks]
    for sep, toks in segments:
        if ASSIGNMENT.match(toks[0]):
            return [], "sets a shell variable"
        # Подстановка в аргументах — только то, что выводит хеш: иначе её вывод (например,
        # `git show HEAD:payload.txt` из PR) превратится в флаги внешней команды.
        if sep == "$(" and not (toks[0] == "git" and toks[1:2] in (["merge-base"], ["rev-parse"])):
            return [], "passes the output of a command other than git merge-base/rev-parse as arguments"
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
