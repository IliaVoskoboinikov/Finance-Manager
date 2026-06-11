# Testing Requirements

## Frameworks
*   **JUnit 4:** Standard for unit tests.
*   **MockK:** For mocking dependencies.
*   **AssertJ:** For fluent assertions.
*   **Turbine:** (If applicable) for testing Flow.

## Coverage Expectations
*   **UseCases:** 100% logic coverage.
*   **ViewModels:** Cover state transitions and event handling.
*   **Repositories:** Cover data mapping and error handling logic.

## Test Scenarios
*   **Happy Path:** Standard successful execution.
*   **Edge Cases:** Empty lists, null values, max/min values.
*   **Failure Scenarios:** Network timeouts, 401 Unauthorized, Database constraints.

## Verification
*   Always run `./gradlew testDebugUnitTest` before completion.
*   Never mark a task complete if tests fail.
