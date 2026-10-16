# Release & Verification Process

## Mandatory Verification
Before marking a task as complete, you MUST run:
```bash
# Run all checks for modified modules
./gradlew :feature:<name>:impl:check

# Run unit tests (pure JVM modules have only `test`)
./gradlew testDebugUnitTest
./gradlew :core:domain:test :konsist:test :lint:test

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
- [ ] New non-UI logic has tests (CI gate `koverVerifyFull`: ≥ 95% lines of testable code).
- [ ] Dependencies changed → `./gradlew :app:dependencyGuardBaseline` re-recorded.
- [ ] Navigation changed → `./gradlew navDump` re-recorded.

## Self-review
For a non-trivial design you propose and for the report on a non-trivial change. Skip it for
trivial edits and plain answers. Write only concrete findings about this change; leave out a
vector with nothing real in it — no "no leaks found" filler.

1. **Faults** in the code you wrote: bugs, races, leaks, coroutine/Flow lifecycle (scope and
   cancellation, `stateIn` + `WhileSubscribed`, collecting outside `collectAsStateWithLifecycle()`),
   un-rollbackable side effects inside a DB transaction, error paths that bypass `DomainResult`.
2. **Edge cases** this app actually hits:
   *   process death — the back stack restores from `@Serializable` `NavKey`s, ViewModel arguments
       come from the key through an assisted factory, in-progress input survives
       (`rememberSaveable` / ViewModel state);
   *   configuration change, including a language switch (`feature:languages` applies app locales,
       the Activity is recreated);
   *   offline / flaky network — `NoInternetException` → `NetworkUnavailable`, Room stays the
       source of truth, `SyncWorker` returns `Result.retry()`;
   *   auth — `401` → `Unauthorized`, guest mode → `GuestModeBlocked`;
   *   KeyStore key lost or invalidated — `CryptoManager.decrypt` throws `GeneralSecurityException`;
       the caller recovers instead of crashing;
   *   money is `BigDecimal` (never `Double`), time is `Instant` with the zone applied at the UI edge;
   *   sync conflicts — `localId`/`serverId`, `PENDING_DELETE`, last-write-wins,
       category → account → transaction order.
3. **Improvements:** design and SOLID issues, a simpler alternative, follow-up cards worth proposing.

## Completion Report
Provide a summary including:
1. **Summary of changes:** What was done.
2. **Architecture impact:** Any changes to module graph or core layers.
3. **Test coverage:** List of new tests.
4. **Self-review:** Faults, edge cases, improvements (see above).
5. **Instruction updates:** Rules added to or changed in the agent instructions (`docs/agents/*.md`, root instruction files), if any.
6. **Task:** Card key and title (`FM-N <title>`), its status, proposed branch `feature/FM-N-…`, commit message ending with `(FM-N)`, drafts of follow-up cards (see `task-board.md`).
