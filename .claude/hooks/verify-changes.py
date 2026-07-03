#!/usr/bin/env python3
"""Stop hook: run ktlintCheck + detekt when there are uncommitted Kotlin changes.

Fast by design — it inspects `git status`, maps changed .kt/.kts files to their
Gradle modules, and runs `:module:ktlintCheck` for just those, plus the project's
aggregate `detekt` task (detekt has no per-module task here). Nothing changed →
instant exit, so plain Q&A turns are never taxed. On failure the output is handed
back to Claude to fix. Loops are prevented via `stop_hook_active`.

Android Lint and unit tests are intentionally NOT run here (too slow for a
per-turn hook) — Claude still runs them before declaring a task done.
"""
import json
import subprocess
import sys
from pathlib import Path

TASK = "ktlintCheck"
# Top-level dirs that contain buildable Gradle modules under a `/src/` layout.
MODULE_ROOTS = ("core/", "feature/", "app/", "sync/", "lint/")


def read_input() -> dict:
    try:
        return json.load(sys.stdin)
    except Exception:
        return {}


def changed_modules(repo: Path) -> list[str]:
    try:
        out = subprocess.run(
            ["git", "status", "--porcelain", "--no-renames"],
            cwd=repo, capture_output=True, text=True, timeout=15,
        ).stdout
    except Exception:
        return []

    modules: set[str] = set()
    for line in out.splitlines():
        path = line[3:].strip().strip('"')
        if not path.endswith((".kt", ".kts")):
            continue
        if path.startswith("build-logic/"):
            continue  # separate included build
        if "/src/" not in path or not path.startswith(MODULE_ROOTS):
            continue
        module = path.split("/src/", 1)[0]
        modules.add(":" + module.replace("/", ":"))
    return sorted(modules)


def main() -> int:
    data = read_input()
    if data.get("stop_hook_active"):
        return 0  # avoid re-entrancy loops

    repo = Path(data.get("cwd") or ".").resolve()
    modules = changed_modules(repo)
    if not modules:
        return 0

    # Per-module ktlintCheck for changed modules + project-wide detekt (aggregate).
    tasks = [f"{m}:{TASK}" for m in modules] + ["detekt"]
    proc = subprocess.run(
        ["./gradlew", *tasks, "--console=plain", "-q"],
        cwd=repo, capture_output=True, text=True,
    )
    if proc.returncode == 0:
        return 0

    report = (proc.stdout + "\n" + proc.stderr).strip()[-4000:]
    print(json.dumps({
        "decision": "block",
        "reason": (
            "ktlintCheck/detekt failed after changes in module(s): "
            f"{', '.join(modules)}.\n"
            "Fix the violations below (ktlint style issues can be auto-fixed with "
            "`./gradlew <module>:ktlintFormat`; detekt reports at "
            "build/reports/detekt/), then continue.\n\n"
            f"{report}"
        ),
    }))
    return 0


if __name__ == "__main__":
    sys.exit(main())
