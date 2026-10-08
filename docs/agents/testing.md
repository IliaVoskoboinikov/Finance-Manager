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
*   **UseCases:** 100% logic coverage.
*   **ViewModels:** Cover state transitions and event handling.
*   **Repositories:** Cover data mapping and error handling logic.

## Test Scenarios
*   **Happy Path:** Standard successful execution.
*   **Edge Cases:** Empty lists, null values, max/min values.
*   **Failure Scenarios:** Network timeouts, 401 Unauthorized, Database constraints.

## Verification
*   Always run `./gradlew testDebugUnitTest` before completion. Pure JVM modules (`:core:domain`,
    `:konsist`, `:lint`) have no `testDebugUnitTest` — run their `test` task (CI runs `./gradlew test`).
*   Never mark a task complete if tests fail.
