# Finance Manager - Agent Instructions

This document is the entry point for all AI agent operations in this repository. It includes specialized instructions for different aspects of the project.

---

## 🎯 General Principles

*   **Preserve Architecture:** Strictly follow the defined project structure and patterns (Clean Architecture, MVVM, UDF).
*   **Minimal Impact:** Implement the smallest possible change to satisfy requirements.
*   **Consistency:** Mirror existing code style, naming conventions, and patterns.
*   **Safety First:** NEVER use `run_shell_command` with `sed`, `awk`, etc. Use `write_file` or `replace_file_content`.

---

## 📖 Detailed Guidelines

The following documents contain mandatory rules for specific areas of development:

@./docs/agents/architecture.md
@./docs/agents/module-rules.md
@./docs/agents/coding-style.md
@./docs/agents/testing.md
@./docs/agents/compose.md
@./docs/agents/navigation.md
@./docs/agents/networking.md
@./docs/agents/database.md
@./docs/agents/coroutines.md
@./docs/agents/security.md
@./docs/agents/di.md
@./docs/agents/release-process.md

---

## 🚀 Agent Workflow

1.  **Requirement Discovery:** Analyze the task and locate relevant modules/files.
2.  **Pattern Matching:** Find similar existing implementations to mirror.
3.  **Implementation:** Write clean, modular code following the linked guidelines.
4.  **Verification:** Execute all mandatory commands in `release-process.md`.
5.  **Summary:** Provide a concise completion report as specified in `release-process.md`.
