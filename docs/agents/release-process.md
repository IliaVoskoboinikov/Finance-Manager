# Release & Verification Process

## Mandatory Verification
Before marking a task as complete, you MUST run:
```bash
# Run all checks for modified modules
./gradlew :feature:<name>:impl:check

# Run unit tests
./gradlew testDebugUnitTest

# Run static analysis
./gradlew ktlintCheck
./gradlew detekt
./gradlew lint
```

## Completion Checklist
- [ ] Code compiles without errors.
- [ ] All unit tests pass.
- [ ] Static analysis (Ktlint, Detekt, Lint) passes.
- [ ] KDoc added for new public APIs.
- [ ] Module `README.md` updated/created.
- [ ] `@Preview` added for new UI components.

## Completion Report
Provide a summary including:
1. **Summary of changes:** What was done.
2. **Architecture impact:** Any changes to module graph or core layers.
3. **Test coverage:** List of new tests.
4. **Risks:** Potential side effects or limitations.
