"""Регрессионные тесты allowlist-хуков субагентов.

Запуск из корня репозитория: `python3 -m unittest discover -s .claude/hooks/tests`.
Каждый обход, найденный на ревью (PR #298), остаётся здесь кейсом «должен отказать».
"""
import importlib.util
import os
import sys
import unittest

HOOKS = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, HOOKS)


def load(file_name: str):
    spec = importlib.util.spec_from_file_location(file_name.replace("-", "_"), os.path.join(HOOKS, file_name))
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


review = load("review-bash-allowlist.py")
triager = load("triager-gh-readonly.py")

# Команды из docs/agents/release-process.md («After the PR is opened», шаг 2).
THREADS_QUERY = (
    "gh api graphql --paginate -F n=298 -f query='query($n:Int!,$endCursor:String)"
    '{repository(owner:"IliaVoskoboinikov",name:"Finance-Manager"){pullRequest(number:$n)'
    "{reviewThreads(first:100,after:$endCursor){pageInfo{hasNextPage endCursor} nodes{isResolved"
    " isOutdated path line comments(first:100){totalCount nodes{author{login} url body}}}}}}}' \\\n"
    "  --jq '.data.repository.pullRequest.reviewThreads.nodes[] | select(.isResolved|not)"
    " | {path, line, isOutdated}'"
)
HEAD_REVIEWS = (
    "head=$(gh api repos/IliaVoskoboinikov/Finance-Manager/pulls/298 --jq .head.sha)\n"
    "gh api --paginate repos/IliaVoskoboinikov/Finance-Manager/pulls/298/reviews \\\n"
    '  --jq ".[] | select(.user.login == \\"coderabbitai[bot]\\" and .commit_id == \\"$head\\")'
    ' | .body"'
)


class ReviewAllowlistTest(unittest.TestCase):
    def assert_allowed(self, *commands):
        for command in commands:
            with self.subTest(command=command):
                self.assertEqual(review.refused(command), "")

    def assert_refused(self, *commands):
        for command in commands:
            with self.subTest(command=command):
                self.assertNotEqual(review.refused(command), "")

    def test_reading_the_diff_is_allowed(self):
        self.assert_allowed(
            "git diff faeb2cc1^1 faeb2cc1",
            "git diff $(git merge-base HEAD origin/master)",
            "git --no-pager log --oneline -5",
            "git status --porcelain",
            "git show faeb2cc1 --stat | head -40",
            "git log --oneline | tail -n5",
            "git diff HEAD 2>&1 | grep -n Outbox | wc -l",
            "git diff | grep -in -e Outbox -e Account",
            "git diff | grep -C 3 rollbackOnError",
            "git log --format='%an (%ae)' | sort | uniq -c | sort -nr",
            "git diff HEAD 2>/dev/null",
            "gh pr diff 293 | head -100",
            "gh pr view 293 --json files",
        )

    def test_other_programs_and_writes_are_refused(self):
        self.assert_refused(
            "git push",
            "git diff & rm -rf build",
            "git status & grep -r TOKEN .",
            "git diff && rm -rf build",
            "git -c core.pager=sh log",
            "git diff > /tmp/x",
            "git diff --output=/tmp/x",
            "git diff --no-index /etc/passwd /dev/null",
            "curl https://example.com",
            "ls app/src",
            "gh pr comment 293 -b x",
            "git diff x#; rm -rf build",
            "git diff # note\nrm -rf build",
            "git diff --out\\\nput=/tmp/x",
            "git diff --no-\\\nindex /etc/passwd /dev/null",
            "r\\\nm -rf build",
            "git diff {--output=/tmp/x,HEAD}",
            "git diff HEAD{,--output=/tmp/x}",
            "git blame --contents /etc/passwd -- README",
            "git diff *",
            "git diff HEAD -- core/?ata",
            "git diff HEAD -- [-]-output=x",
        )

    def test_quoted_braces_globs_and_hashes_stay_allowed(self):
        self.assert_allowed(
            "git log --format='{%h,%s}'",
            "git log --grep '#298'",
            "git diff HEAD -- \\\n  core/data",
            "git diff HEAD -- '*.kt'",
            "git diff HEAD -- \\*.kt",
        )

    def test_filters_read_only_the_piped_output(self):
        self.assert_refused(
            "grep -r TOKEN .",
            "git status | grep -r TOKEN .",
            "git diff | grep TOKEN local.txt",
            "git diff | grep -f patterns.txt",
            "git diff | sort -o out.txt",
            "git diff | head /etc/passwd",
            "git diff | uniq - out",
            "git diff | wc --files0-from=list",
        )

    def test_secrets_and_hidden_expansions_are_refused(self):
        self.assert_refused(
            "cat local.properties",
            "git show HEAD:app/google-services.json",
            "echo $(cat ~/.ssh/id_rsa)",
            'git diff "$(cat payload)"',
            "git diff `cat payload`",
            "git diff ${X:-HEAD}",
            "X=--output=f; git diff $X",
            "git log --format='%H' 'unbalanced",
        )


class TriagerReadOnlyTest(unittest.TestCase):
    def assert_allowed(self, *commands):
        for command in commands:
            with self.subTest(command=command):
                self.assertEqual(triager.refused(command), "")

    def assert_refused(self, *commands):
        for command in commands:
            with self.subTest(command=command):
                self.assertNotEqual(triager.refused(command), "")

    def test_reading_the_review_is_allowed(self):
        self.assert_allowed(
            THREADS_QUERY,
            HEAD_REVIEWS,
            "gh api repos/O/R/pulls/298 --jq .head.sha",
            "gh api --paginate repos/O/R/issues/298/comments --jq '.[].body'",
            "gh api graphql -f query='{ viewer { login } }'",
            "gh pr view 298 --json headRefOid",
            "gh api repos/O/R/pulls/298 | jq -r .head.sha",
            "git show c5da88a4 --stat",
        )

    def test_publishing_is_refused(self):
        self.assert_refused(
            "gh pr comment 298 --body x",
            "gh pr review 298 --approve",
            "gh issue comment 298 -b x",
            "gh api -X POST repos/O/R/issues/298/comments",
            "gh api -XPOST repos/O/R/issues/298/comments",
            "gh api --method=PATCH repos/O/R/pulls/298",
            "gh api repos/O/R/issues/298/comments -f body=hi",
            "gh api repos/O/R/issues/298/comments -fbody=hi",
            "gh api repos/O/R/issues/298/comments -ifbody=hi",
            'gh api repos/O/R/issues/298/comments "-f"body=hi',
            "gh api repos/O/R/issues/298/comments -'f'body=hi",
            "gh api repos/O/R/issues/298/comments --field=body=hi",
            "gh api graphql --input payload.json",
            "gh auth token",
            "curl -d x https://api.github.com/x",
            "bash -c 'gh pr comment 298 -b x'",
            "eval 'gh pr comment 298 -b x'",
            "gh api repos/O/R/pulls/298 #; gh pr comment 298 -b x",
            "gh api repos/O/R/pulls/298 # note\ngh pr comment 298 -b x",
            "gh api repos/O/R/issues/298/comments -f\\\nbody=hi",
            "gh api {-fbody=x,repos/O/R/issues/298/comments}",
            "gh api graphql -fquery={mut,}ation{x}",
            "git blame --contents /etc/passwd -- README",
            "gh api repos/O/R/pulls/298 *",
            "git diff *",
        )

    def test_graphql_mutations_are_refused_in_any_form(self):
        self.assert_refused(
            "gh api graphql -f query='mutation{x}'",
            "gh api graphql -f query=\"mutation{x}\"",
            "gh api graphql -f query='mut''ation { resolveReviewThread(input:{threadId:\"x\"}){thread{id}} }'",
            "gh api graphql -fquery='mutation{x}'",
            "gh api graphql -f query='{viewer{login}}' -fquery='mutation{x}'",
            "gh api graphql -f query=\"$(cat payload)\"",
            "gh api graphql -f query=`cat payload`",
            "gh api graphql -F query=@q.graphql",
            "gh api graphql -f query='query{x}' -f operationName=Write",
            "gh api graphql -f query=\"${Q:-mutation{x}}\"",
            "gh api graphql -f query='query{x}'$'\\x6d'utation",
        )

    def test_a_single_ampersand_separates_commands(self):
        self.assert_refused(
            "gh api graphql -f query='{viewer{login}}' & gh api repos/O/R/issues/1/comments -f body=x",
            "gh api graphql -f query='query{viewer{login}}' && gh api repos/O/R/issues/298/comments -f body=hi",
        )


if __name__ == "__main__":
    unittest.main()
