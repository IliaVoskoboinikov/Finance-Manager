"""Тесты чистой логики board.py и ci_failure.py: разбор веток, Closes, статусы PR, сводка.

Запуск: python3 -m unittest discover -s .github/scripts -p 'test_*.py'
Обращений к GitHub тесты не делают.
"""
import unittest
from datetime import datetime, timedelta, timezone

import board
import ci_failure

NOW = datetime(2026, 10, 12, 6, 0, tzinfo=timezone.utc)


def card(number, title="Задача", status="Backlog", labels=(), state="OPEN", state_reason=None,
         status_days_ago=0, closed_days_ago=None):
    return board.Card(
        number=number,
        title=title,
        state=state,
        state_reason=state_reason,
        status=status,
        labels=frozenset(labels),
        status_changed_at=NOW - timedelta(days=status_days_ago),
        closed_at=None if closed_days_ago is None else NOW - timedelta(days=closed_days_ago),
    )


def closed(number, title="Задача", days_ago=1, reason="COMPLETED", labels=()):
    return card(number, title, status="Done", labels=labels, state="CLOSED", state_reason=reason,
                closed_days_ago=days_ago)


class IssueFromBranchTest(unittest.TestCase):
    def test_extracts_number_from_task_branch(self):
        self.assertEqual(board.issue_from_branch("feature/FM-42-fix_navigation"), 42)
        self.assertEqual(board.issue_from_branch("refs/heads/feature/FM-7-x"), 7)
        self.assertEqual(board.issue_from_branch("feature/FM-123"), 123)

    def test_ignores_other_branches(self):
        # Ветка задачи в корне (без feature/) — не по схеме.
        for ref in ("FM-42-fix_navigation", "feature/task-board", "master", "renovate/gradle-9.x",
                    "feature/FM-", "feature/FM-x-1", "feature/fm-42-lower", "fix/FM-42-y",
                    "feature/xFM-42-y", "feature/FM42-no-dash"):
            self.assertIsNone(board.issue_from_branch(ref), ref)


class EnsureClosesTest(unittest.TestCase):
    def test_keeps_body_when_task_already_closed_by_pr(self):
        for body in ("Closes #5", "fixes #5 and more", "Resolved: #5", "CLOSES IliaVoskoboinikov/Finance-Manager#5"):
            self.assertIsNone(board.ensure_closes(body, 5), body)

    def test_fills_empty_template_line(self):
        body = "Closes #\n\n## Чек-лист\n- [ ] x"
        self.assertEqual(board.ensure_closes(body, 12), "Closes #12\n\n## Чек-лист\n- [ ] x")

    def test_prepends_when_missing(self):
        self.assertEqual(board.ensure_closes("Описание", 3), "Closes #3\n\nОписание")
        self.assertEqual(board.ensure_closes("", 3), "Closes #3\n")

    def test_other_issue_does_not_count(self):
        self.assertEqual(board.ensure_closes("Closes #4", 5), "Closes #5\n\nCloses #4")


class PrTargetStatusTest(unittest.TestCase):
    def test_open_ready_pr_is_in_review(self):
        self.assertEqual(board.pr_target_status("OPEN", False), "In Review")

    def test_draft_and_closed_pr_stay_in_progress(self):
        self.assertEqual(board.pr_target_status("OPEN", True), "In Progress")
        self.assertEqual(board.pr_target_status("CLOSED", False), "In Progress")

    def test_merged_pr_is_left_to_built_in_workflows(self):
        self.assertIsNone(board.pr_target_status("MERGED", False))


class EscapeCommandTest(unittest.TestCase):
    def test_neutralizes_workflow_command_injection(self):
        self.assertEqual(board.escape_command("a%b\nc\r"), "a%25b%0Ac%0D")


class DigestTest(unittest.TestCase):
    def test_full_digest(self):
        cards = [
            card(1, "Эпик", status="In Progress", labels=["epic"]),
            card(2, "Давно в работе", status="In Progress", status_days_ago=9),
            card(3, "Свежая", status="In Progress", status_days_ago=2),
            card(4, "На ревью", status="In Review"),
            card(5, "Срочный баг в бэклоге", labels=["bug"]),
            closed(6, "Сделана"),
            closed(7, "Эпик закрыт", labels=["epic"]),
        ]
        text = board.build_digest(cards, NOW)
        self.assertTrue(text.startswith("📋 Доска: 05.10 – 12.10\n"))
        self.assertIn("🔨 В работе (2):\n• FM-2 Давно в работе — ⏳ 9 дн.\n• FM-3 Свежая\n", text)
        self.assertIn("👀 На ревью (1):\n• FM-4 На ревью\n", text)
        self.assertIn("✅ Сделано за неделю (2):\n• FM-6 Сделана\n• FM-7 Эпик закрыт\n", text)
        # Бэклог и открытые эпики в отчёт о неделе не попадают.
        self.assertNotIn("FM-5", text)
        self.assertNotIn("FM-1 ", text)
        self.assertTrue(text.endswith(board.PROJECT_URL))

    def test_empty_board(self):
        text = board.build_digest([], NOW)
        self.assertIn("🔨 В работе: ничего", text)
        self.assertIn("👀 На ревью: ничего", text)
        self.assertIn("✅ Сделано за неделю: ничего", text)

    def test_done_is_completed_within_window(self):
        cards = [
            closed(1, "Вчера"),
            closed(2, "Шесть дней назад", days_ago=6),
            closed(3, "Неделю назад", days_ago=7),
            closed(4, "Не будем делать", reason="NOT_PLANNED"),
            closed(5, "Дубль", reason="DUPLICATE"),
        ]
        text = board.build_digest(cards, NOW)
        self.assertIn("✅ Сделано за неделю (2):\n• FM-1 Вчера\n• FM-2 Шесть дней назад\n", text)

    def test_long_section_is_cut_with_remainder(self):
        cards = [closed(n) for n in range(1, board.SECTION_LIMIT + 4)]
        text = board.build_digest(cards, NOW)
        self.assertIn(f"✅ Сделано за неделю ({board.SECTION_LIMIT + 3}):", text)
        self.assertIn(f"• FM-{board.SECTION_LIMIT} Задача\n• и ещё 3\n", text)
        self.assertNotIn(f"FM-{board.SECTION_LIMIT + 1} ", text)

    def test_long_digest_is_truncated_to_telegram_limit(self):
        cards = [card(n, "Очень длинное название " * 5, status="In Progress") for n in range(1, 200)]
        self.assertLessEqual(len(board.build_digest(cards, NOW)), board.TELEGRAM_LIMIT)

    def test_title_is_shortened(self):
        self.assertEqual(len(board.shorten("а" * 100)), 60)
        self.assertEqual(board.shorten("коротко"), "коротко")


class ParseTimestampTest(unittest.TestCase):
    def test_parses_github_timestamps(self):
        self.assertEqual(board.parse_ts("2026-10-09T09:05:15Z"), datetime(2026, 10, 9, 9, 5, 15, tzinfo=timezone.utc))
        self.assertIsNone(board.parse_ts(None))


class CiFailureTest(unittest.TestCase):
    RUN = {
        "name": "CI", "run_number": 166, "conclusion": "failure", "head_sha": "4212b22d3ead",
        "html_url": "https://github.com/o/r/actions/runs/1",
        "head_commit": {"message": "Merge branch 'x'\n\nподробности"},
    }

    def test_only_latest_run_decides(self):
        run = {"id": 10}
        self.assertTrue(ci_failure.is_latest_run(run, {"id": 10}))
        self.assertTrue(ci_failure.is_latest_run(run, None))
        # Более свежий прогон (даже ещё идущий) — старый результат не должен трогать issue.
        self.assertFalse(ci_failure.is_latest_run(run, {"id": 11}))

    def test_title_is_per_workflow(self):
        self.assertEqual(ci_failure.issue_title("CI"), "Красный master: CI")

    def test_failed_jobs_list_only_failures(self):
        jobs = [{"name": "run-ktlint", "conclusion": "failure"}, {"name": "build-app", "conclusion": "success"}]
        self.assertEqual(ci_failure.failed_jobs_markdown(jobs), "- run-ktlint")
        self.assertIn("не удалось определить", ci_failure.failed_jobs_markdown([]))

    def test_issue_body_follows_bug_template(self):
        body = ci_failure.issue_body(self.RUN, [{"name": "run-ktlint", "conclusion": "failure"}], "o/r")
        for section in ("### Что происходит", "### Как воспроизвести", "### Ожидание", "### Источник"):
            self.assertIn(section, body)
        self.assertIn("Merge branch 'x'", body)
        self.assertNotIn("подробности", body)
        self.assertIn("[4212b22](https://github.com/o/r/commit/4212b22d3ead)", body)


if __name__ == "__main__":
    unittest.main()
