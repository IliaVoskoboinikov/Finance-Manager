"""Тесты чистой логики board.py и ci_failure.py: разбор веток, Closes, статусы PR, сводка.

Запуск: python3 -m unittest discover -s .github/scripts -p 'test_*.py'
Обращений к GitHub тесты не делают.
"""
import unittest
from datetime import datetime, timedelta, timezone

import board
import ci_failure

NOW = datetime(2026, 10, 12, 6, 0, tzinfo=timezone.utc)


def card(number, title="Задача", status="Backlog", priority=None, labels=(), state="OPEN",
         updated_days_ago=0, closed_days_ago=None, sub=(0, 0)):
    return board.Card(
        number=number,
        title=title,
        state=state,
        status=status,
        priority=priority,
        labels=frozenset(labels),
        updated_at=NOW - timedelta(days=updated_days_ago),
        closed_at=None if closed_days_ago is None else NOW - timedelta(days=closed_days_ago),
        sub_total=sub[1],
        sub_done=sub[0],
    )


class IssueFromBranchTest(unittest.TestCase):
    def test_extracts_number_from_task_branch(self):
        self.assertEqual(board.issue_from_branch("FM-42-fix_navigation"), 42)
        self.assertEqual(board.issue_from_branch("refs/heads/FM-7-x"), 7)
        self.assertEqual(board.issue_from_branch("FM-123"), 123)

    def test_ignores_other_branches(self):
        for ref in ("feature/task-board", "master", "renovate/gradle-9.x", "FM-", "FM-x-1",
                    "fm-42-lower", "xFM-42-y", "FM42-no-dash"):
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
            card(1, "Эпик", status="In Progress", labels=["epic"], sub=(2, 5)),
            card(2, "В работе", status="In Progress", priority="P1", updated_days_ago=9),
            card(3, "На ревью", status="In Review", priority="P1"),
            card(4, "Срочный баг", priority="P0", labels=["bug"]),
            card(5, "Обычная", priority="P2"),
            card(6, "Без приоритета"),
            card(7, "Ждёт бэкенд", priority="P0", labels=["needs-backend"]),
            card(8, "Закрыта", status="Done", state="CLOSED", closed_days_ago=2),
            card(9, "Давно закрыта", status="Done", state="CLOSED", closed_days_ago=30),
        ]
        text = board.build_digest(cards, NOW)
        self.assertIn("неделя 42", text)
        self.assertIn("🔨 В работе (1): FM-2 В работе", text)
        self.assertIn("👀 На ревью (1): FM-3 На ревью", text)
        self.assertIn("⏳ Без движения 7+ дней: FM-2 (9 дн.)", text)
        # Следующая — P0 без блокирующих флагов: FM-7 ждёт бэкенд и не годится.
        self.assertIn("➡️ Дальше: FM-4 Срочный баг (P0)", text)
        self.assertIn("🔴 P0 в Backlog: 2", text)
        self.assertIn("🆕 Без приоритета: 1", text)
        self.assertIn("⛔ Заблокировано: 1 (needs-backend: 1)", text)
        self.assertIn("🧩 FM-1 Эпик — 2/5", text)
        self.assertIn("✅ Закрыто за неделю: 1", text)
        self.assertTrue(text.endswith(board.PROJECT_URL))

    def test_empty_board(self):
        text = board.build_digest([], NOW)
        self.assertIn("🔨 В работе: ничего", text)
        self.assertNotIn("➡️", text)
        self.assertNotIn("🆕", text)
        self.assertIn("✅ Закрыто за неделю: 0", text)

    def test_epic_is_not_counted_as_work(self):
        text = board.build_digest([card(1, "Эпик", status="In Progress", labels=["epic"])], NOW)
        self.assertIn("🔨 В работе: ничего", text)
        self.assertNotIn("Без приоритета", text)

    def test_unprioritized_card_is_proposed_last(self):
        cards = [card(1, "Без приоритета"), card(2, "P3", priority="P3")]
        self.assertIn("➡️ Дальше: FM-2 P3 (P3)", board.build_digest(cards, NOW))

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
