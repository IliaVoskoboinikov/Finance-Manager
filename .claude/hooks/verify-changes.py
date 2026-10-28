#!/usr/bin/env python3
"""Stop hook: format, then run ktlintCheck + detekt when there are uncommitted Kotlin changes.

Fast by design — it inspects `git status`, maps changed .kt/.kts files (sources and
module build scripts) to their Gradle modules, runs `:module:ktlintFormat` for just
those (style is fixed, not reported), then `:module:ktlintCheck` plus the project's
aggregate `detekt` task (detekt has no per-module task here). Format runs as a separate
Gradle call so the check always sees the formatted files. A clean tree or plan mode →
instant exit; while Kotlin changes sit uncommitted, every stop is checked.

On failure the output is handed back to Claude to fix (`decision: block`). The stop
after that fix (`stop_hook_active`) is checked again, but a failure there is only
reported to the user — blocking twice would never let the turn end. A check that could
not run (git failed, Gradle timed out) is reported too, never passed off as green.

Android Lint and unit tests are intentionally NOT run here (too slow for a
per-turn hook) — Claude still runs them before declaring a task done.
"""
import json
import os
import subprocess
import sys
from pathlib import Path
from typing import Optional

TASK = "ktlintCheck"
FORMAT_TASK = "ktlintFormat"
# Top-level dirs that contain buildable Gradle modules.
MODULE_ROOTS = ("core/", "feature/", "app/", "sync/", "lint/", "konsist/")
# Two Gradle runs must fit the 600 s hook timeout in .claude/settings.json.
GRADLE_TIMEOUT = 270


def read_input() -> dict:
    try:
        return json.load(sys.stdin)
    except Exception:
        return {}


def emit(**fields) -> None:
    print(json.dumps({k: v for k, v in fields.items() if v}))


def tail(text: str, limit: int) -> str:
    return text.strip()[-limit:]


def project_root(data: dict) -> Optional[Path]:
    # The session may stand in a subdirectory or another repo; the hook is about this one.
    env = os.environ.get("CLAUDE_PROJECT_DIR")
    if env:
        return Path(env).resolve()
    try:
        out = subprocess.run(
            ["git", "rev-parse", "--show-toplevel"],
            cwd=data.get("cwd") or ".", capture_output=True, text=True, timeout=15,
        )
    except Exception:
        return None
    return Path(out.stdout.strip()) if out.returncode == 0 else None


def module_of(path: str) -> Optional[str]:
    if not path.endswith((".kt", ".kts")) or not path.startswith(MODULE_ROOTS):
        return None
    if "/src/" in path:
        return path.split("/src/", 1)[0]
    if path.endswith("/build.gradle.kts"):
        return path.rsplit("/", 1)[0]
    return None


def changed_kotlin_files(repo: Path) -> Optional[list]:
    """Changed files that belong to a module; None when git itself failed."""
    try:
        out = subprocess.run(
            # -uall: a new package or module is otherwise one collapsed `dir/` line.
            ["git", "status", "--porcelain", "--no-renames", "--untracked-files=all"],
            cwd=repo, capture_output=True, text=True, timeout=15,
        )
    except Exception:
        return None
    if out.returncode != 0:
        return None
    paths = (line[3:].strip().strip('"') for line in out.stdout.splitlines())
    return [p for p in paths if module_of(p)]


def changed_modules(files: list) -> list:
    return sorted({":" + module_of(f).replace("/", ":") for f in files})


def snapshot(repo: Path, files) -> dict:
    """Contents of the changed files, to tell which ones ktlintFormat rewrote."""
    result = {}
    for f in files:
        try:
            result[f] = (repo / f).read_bytes()
        except OSError:
            result[f] = b""  # deleted file
    return result


def gradle(repo: Path, tasks: list) -> tuple:
    """(exit code, output); the code is None when Gradle did not finish."""
    try:
        proc = subprocess.run(
            ["./gradlew", *tasks, "--continue", "--console=plain", "-q"],
            cwd=repo, capture_output=True, text=True, timeout=GRADLE_TIMEOUT,
        )
    except subprocess.TimeoutExpired:
        return None, f"timed out after {GRADLE_TIMEOUT} s"
    except OSError as e:
        return None, str(e)
    return proc.returncode, proc.stdout + "\n" + proc.stderr


def main() -> int:
    data = read_input()
    if data.get("permission_mode") == "plan":
        return 0  # plan mode must not rewrite sources
    retry = bool(data.get("stop_hook_active"))

    repo = project_root(data)
    if repo is None or not (repo / "gradlew").exists():
        return 0  # not this project
    files = changed_kotlin_files(repo)
    if files is None:
        emit(systemMessage="verify-changes: git status failed, ktlint/detekt did not run.")
        return 0
    modules = changed_modules(files)
    if not modules:
        return 0

    # Fix style first; whatever ktlint can't fix still fails the check below. The format
    # also touches files the user edits by hand, so the rewritten ones are named.
    before = snapshot(repo, files)
    fmt_code, fmt_out = gradle(repo, [f"{m}:{FORMAT_TASK}" for m in modules])
    # A file the format brings back to HEAD drops out of git status, so compare the union.
    touched = sorted(set(files) | set(changed_kotlin_files(repo) or []))
    after = snapshot(repo, touched)
    reformatted = [f for f in touched if before.get(f) != after[f]]

    # The user only needs to know which files changed under them; Claude gets the details.
    note = f"ktlintFormat rewrote: {', '.join(reformatted)}." if reformatted else ""
    # Format exits non-zero on violations it can't fix — expected; only a Gradle that did
    # not finish means style was left unfixed.
    fmt_note = "" if fmt_code is not None else f"ktlintFormat did not finish ({fmt_out})."

    # Per-module ktlintCheck for changed modules + project-wide detekt (aggregate).
    code, out = gradle(repo, [f"{m}:{TASK}" for m in modules] + ["detekt"])
    if code == 0:
        emit(systemMessage=note)
        return 0

    if code is None:
        problem = f"ktlintCheck/detekt did not finish ({out}); run them by hand."
    else:
        problem = (
            f"ktlintCheck/detekt failed in module(s): {', '.join(modules)}.\n"
            "Fix the violations below (detekt reports at build/reports/detekt/), "
            f"then continue.\n\n{tail(out, 4000)}"
        )

    details = "\n".join(s for s in (note, fmt_note, problem) if s)
    if retry:
        emit(systemMessage=details)
        return 0
    emit(decision="block", reason=details, systemMessage=note)
    return 0


if __name__ == "__main__":
    sys.exit(main())
