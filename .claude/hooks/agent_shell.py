"""Shell command parsing shared by the subagents' allowlist hooks.

A hook that fails with an exception must still refuse: Claude Code treats any exit code
other than 2 as a non-blocking error and runs the command. `run_hook` does that for both.

The hooks of the read-only subagents (`review-bash-allowlist.py`,
`triager-gh-readonly.py`) must judge the command the shell will run, not its text: quotes
(`"-f"body=x`, `'mut''ation'`) and a single `&` hide a command from a regular expression.
`split_command` tokenizes like the shell does (`shlex`, operators as separate tokens, quotes
removed) and cuts the command into simple commands; a hook then allows each one by its
program and arguments, and refuses everything it does not know.

What the shell would expand at run time and `shlex` can't show is refused here: any `$`
the shell expands (outside quotes or inside double quotes — a variable, `${…}`, `$'…'`),
backticks, shell variables set in the command, a comment (`#` starting a word outside
quotes: the shell drops the rest of the line, `shlex` would not), `<(…)`, and `{a,b}` or
globs (`*`, `?`, `[…]`) outside quotes. `$` inside single quotes is a literal (GraphQL
variables). `\⏎` joins words as the shell does.

The one expansion allowed is an unquoted `$(…)` holding a single command, `git merge-base`
or `git rev-parse`: it prints a hash, and the command is checked as a segment of its own.
A separator inside quotes (`cut -d "|"`) still splits the command; that only makes a hook
refuse more, never less.
"""
import json
import re
import shlex
import sys
from typing import Callable, List, Optional, Tuple

SEPARATORS = {"&&", "||", "|", ";", "&", "(", ")"}
ASSIGNMENT = re.compile(r"[A-Za-z_]\w*=")
WORD_START = set(" \t\n;&|()")

Segment = Tuple[str, List[str]]  # (разделитель перед сегментом, токены сегмента)


PUNCTUATION_ONLY = re.compile(r"[();<>|&]+")
OPERATOR = re.compile(r"&&|\|\||>>|>&|&>|<\(|[;&|()<>]")


GIT_READ = {"diff", "log", "show", "merge-base", "status", "rev-parse", "ls-files", "blame"}
# Опции, которыми эти команды пишут файл или читают файл вне git (`blame --contents`).
GIT_FILE_OPTIONS = ("--output", "--no-index", "--contents", "--ignore-revs-file")
# Короткие опции с файлом: `-O <orderfile>` у diff/log/show, `-S <revs-file>` у blame
# (у log `-S` — поиск строки, его не трогаем).
GIT_FILE_SHORT = {"-O": GIT_READ, "-S": {"blame"}}
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


def _expansion_refused(command: str) -> str:
    """Why the raw command has an expansion or a comment the hooks can't judge, or ''.

    Walks the text with the shell's quote rules: inside single quotes nothing is special;
    outside quotes and inside double quotes `$` and backticks expand.
    """
    quote, index = "", 0
    while index < len(command):
        char = command[index]
        previous = command[index - 1] if index else "\n"
        following = command[index + 1:index + 2]
        index += 1
        if quote == "'":
            if char == "'":
                quote = ""
            continue
        if char == "\\":
            index += 1  # экранированный символ — литерал
            continue
        if char == "`":
            return "uses a backtick substitution"
        if char == "$":
            # единственная разрешённая подстановка — `$(` вне кавычек; что внутри, проверит разбор
            if quote or following != "(":
                return "expands a shell variable or substitution (write the value literally)"
            continue
        if quote == '"':
            if char == '"':
                quote = ""
            continue
        if char in "'\"":
            quote = char
        elif char == "#" and previous in WORD_START:
            return "contains a shell comment"
    return ""


def git_read_refused(args: List[str]) -> str:
    """Why `git <args>` is not a plain read, or an empty string (`--no-pager` is dropped)."""
    args = [a for a in args if a != "--no-pager"]
    if not args or args[0] not in GIT_READ:
        return "runs a git command that is not read-only"
    for arg in args:
        if any(arg.startswith(short) and args[0] in commands for short, commands in GIT_FILE_SHORT.items()):
            return "makes git read a file outside the repository"
        name = arg.split("=", 1)[0]
        # git принимает однозначное сокращение длинной опции (`blame --cont=файл`)
        abbreviation = len(name) > 2 and any(o.startswith(name) for o in GIT_FILE_OPTIONS)
        if abbreviation or any(option in arg for option in GIT_FILE_OPTIONS):
            return "makes git write a file or read one outside the repository"
    return ""


# Выражения jq, которые читают окружение (токены) или файлы, а не JSON из stdin.
JQ_OUTSIDE_INPUT = re.compile(r"\$ENV|(?<![.\w$])env\b|\binput_filename\b|\$__loc__|\b(include|import)\b")


def jq_expression_refused(expressions: List[str]) -> str:
    """Why one of the jq expressions reads more than its input (env, files), or ''."""
    if any(JQ_OUTSIDE_INPUT.search(expression) for expression in expressions):
        return "reads the environment or files from a jq expression"
    return ""


def jq_refused(args: List[str], filter_operand: bool = False) -> str:
    """Why a jq expression in the arguments reads more than its input, or ''.

    Looks at `--jq` values and `-q` in any short-option cluster (`-q x`, `-qx`, `-cq x` —
    gh lets boolean shorthands precede it), or at every operand when `filter_operand` (the
    `jq` program, whose operand is the filter).
    """
    expressions = []
    for index, arg in enumerate(args):
        following = args[index + 1] if index + 1 < len(args) else ""
        cluster = re.fullmatch(r"-([A-Za-z]*?)q(.*)", arg)
        if arg == "--jq":
            expressions.append(following)
        elif arg.startswith("--jq="):
            expressions.append(arg[len("--jq="):])
        elif cluster:
            expressions.append(cluster.group(2) or following)
        elif filter_operand and not arg.startswith("-"):
            expressions.append(arg)
    return jq_expression_refused(expressions)


# Фильтры после `|` → (флаги без значения, флаги со значением, сколько операндов можно).
# Файловых операндов нет: фильтр читает только stdin и ничего не пишет (`sort -o`,
# `uniq in out`, `grep -r` отпадают); у grep единственный операнд — шаблон.
READ_FILTERS = {
    "head": ("", "nc", 0),
    "tail": ("", "nc", 0),
    "wc": ("lwcm", "", 0),
    "sort": ("nruf", "kt", 0),
    "uniq": ("cdu", "", 0),
    "grep": ("nivcEFwo", "emABC", 1),
}


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
    command = command.replace("\\\n", "")
    # `\r` и другие управляющие символы shlex считает пробелом, а bash — частью слова.
    if re.search(r"[\x00-\x08\x0b-\x1f\x7f]", command):
        return [], "contains a control character"
    # Комментарий кончается на переводе строки — проверяем до того, как строки станут `;`.
    reason = _expansion_refused(command)
    if reason:
        return [], reason
    command = command.replace("\n", ";")
    unquoted = _unquoted(command)
    # `{a,b}` вне кавычек оболочка размножит в несколько слов, а после shlex этого не видно.
    if BRACE_EXPANSION.search(unquoted):
        return [], "uses a brace expansion"
    # `*`, `?`, `[…]` вне кавычек оболочка заменит именами файлов — в том числе файла
    # `--output=x`, который мог принести сам проверяемый PR.
    if re.search(r"[*?\[]", unquoted):
        return [], "uses an unquoted glob (quote the pattern)"
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
        # станет отдельным сегментом с разделителем `$(`: её вывод оболочка разобьёт на слова.
        if next_token == "(" and token.endswith("$"):
            if ASSIGNMENT.match(token):
                # значение переменной потом попало бы в аргументы, где хук его не видит
                return [], "sets a shell variable"
            current.append(token[:-1] + "SUBSTITUTION")
            outer.append((separator, current))
            separator, current = "$(", []
            index += 1  # пропускаем `(`
            continue
        if token == ")" and outer:
            segments.append((separator, current))
            separator, current = outer.pop()
            continue
        if token in SEPARATORS:
            # Вторая команда в `$(…)` прошла бы мимо правила «только merge-base / rev-parse»,
            # а её вывод стал бы аргументами внешней команды.
            if outer:
                return [], "runs several commands inside a substitution"
            segments.append((separator, current))
            separator, current = token, []
            continue
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


def run_hook(name: str, refused: Callable[[str], str], hint: str) -> int:
    """Read the PreToolUse input, judge the command, return the hook's exit code.

    Exit code 2 refuses the command and shows stderr to the agent. Any failure of the hook
    itself — unreadable input or a bug in `refused` — refuses too: with code 1 the command
    would run.
    """
    try:
        command = json.load(sys.stdin).get("tool_input", {}).get("command", "")
        reason = refused(command)
    except Exception as error:  # noqa: BLE001 — хук обязан закрыться при любой ошибке
        print(f"{name}: the hook failed ({type(error).__name__}), the command is refused", file=sys.stderr)
        return 2
    if reason:
        print(f"{name}: the command {reason}. {hint}", file=sys.stderr)
        return 2
    return 0
