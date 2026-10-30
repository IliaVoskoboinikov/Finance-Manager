# Testing Requirements

## Frameworks
*   **JUnit 4:** Standard for unit tests.
*   **MockK:** For mocking dependencies.
*   **AssertJ:** For fluent assertions.
*   **kotlinx-coroutines-test:** `runTest`, `UnconfinedTestDispatcher`, `Dispatchers.setMain`/`resetMain`.
    Flows are tested without Turbine (it is not in the version catalog): a `StateFlow` with
    `WhileSubscribed` is started by a separate collector coroutine
    (`launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect {} }`), cancelled at the end.
*   **Robolectric:** Android framework on the JVM (in-memory Room DAOs, WorkManager, `ConnectivityManager`);
    runs inside `testDebugUnitTest`, no emulator.
*   **androidx.work:work-testing:** `TestListenableWorkerBuilder` for `CoroutineWorker`.
*   **Gradle TestKit:** functional tests of the `build-logic` convention plugins (`GradleRunner`).
*   JUnit, MockK, AssertJ and coroutines-test come from the `unit-test` bundle added by the convention
    plugins; Robolectric / `work-testing` / `androidx.test:core` are declared per module. Details: `docs/testing.md`.

## Coverage Expectations
*   **UseCases:** every branch of real logic has a behaviour test (plain pass-throughs need none).
*   **ViewModels:** Cover state transitions and event handling.
*   **Repositories:** Cover data mapping and error handling logic.
*   **CI gate:** aggregate line coverage of testable code ≥ 70% (`./gradlew :koverVerifyFull`, rule and
    exclusions in the root `build.gradle.kts`). UI (`@Composable`/`@Preview`, `*ScreenKt`, Activities,
    `*FeatureImpl`) and generated code are excluded — everything else counts.
*   **Target:** 90% — a direction, not a gate. Never add or reshape production code just to make it
    coverable, and never write tests that only execute lines without asserting behaviour. New logic
    ships with behaviour tests; trivial code may stay untested while the total is above the gate.
    With the real coverage far above the gate, Kover won't catch new logic without tests — the
    review does (self-review loop, CodeRabbit).
*   **Changing the gate or the target:** the gate lives in `minLineCoverage` (root
    `build.gradle.kts`), the target only in docs; both are repeated, so update every copy in the
    same change — `git grep -nE "<old> ?%"` finds `CLAUDE.md`, `README.md` + `README.ru.md`,
    `CONTRIBUTING.md`, `docs/testing.md`, `docs/agents/*.md`, the KDoc above `kover {}`.

## Test Scenarios
*   **Happy Path:** Standard successful execution.
*   **Edge Cases:** Empty lists, null values, max/min values.
*   **Failure Scenarios:** Network timeouts, 401 Unauthorized, Database constraints.

## Bug fixes: test first
*   Before touching the fix, write a test that reproduces the bug, run it and see it fail for
    the reason the card describes. Then fix the code until it passes; don't weaken the test or
    its assertions to make it green.
*   The test stays as the regression guard and names the behaviour, not the card
    (`passes are capped so an endless supply cannot loop forever`, not `FM-278`).
*   A bug that can't be reproduced on the JVM even under Robolectric (Compose UI, a device-only
    API) gets no fake test: say so in the report and describe how the fix was checked on the
    emulator.

## Verification
*   Always run `./gradlew testDebugUnitTest` before completion. Pure JVM modules (`:core:domain`,
    `:konsist`, `:lint`) have no `testDebugUnitTest` — run their `test` task (CI runs `./gradlew test`).
*   Never mark a task complete if tests fail.
