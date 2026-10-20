# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

These rules are mandatory and override any default behavior.

## What this is

**Finance Manager** — an offline-first Android app for personal finance (income,
expenses, accounts, categories, operation history, background sync, basic
analytics). A pet-project showcasing a well-structured, modern multi-module
Android codebase. Status: 🚧 pre-release, **no real users yet** (this matters for
DB migration rules below).

## Tech stack

- **Kotlin, Java 11**, AGP, `compileSdk 36`, `minSdk 26` (`build-logic/convention/.../Const.kt`).
  Library versions live only in `gradle/libs.versions.toml` and Renovate bumps them — read them
  there, never copy version numbers into docs (they go stale).
- **UI:** Jetpack Compose + Material 3 (custom components in `core:uikit`).
- **Navigation:** Navigation 3 (`androidx.navigation3`) — `NavKey` + `NavDisplay`, keys are
  serialized with kotlinx.serialization.
- **Architecture:** Clean Architecture + MVVM + Unidirectional Data Flow (UDF), offline-first.
- **DI:** Dagger Hilt. **Async:** Coroutines + Flow.
- **Storage:** Room (`FinanceManagerDatabase`, SSOT) + DataStore.
- **Network:** Retrofit + OkHttp + Gson; custom interceptors (Auth, Retry, NetworkConnection, Logging).
- **Background:** WorkManager (`:sync`; shared `DelegatingWorker` infra in `core:workmanager`).
  **Security:** `core:security` `CryptoManager` (AES/GCM + KeyStore).
- **Quality:** Detekt (+ compose-rules), ktlint, Android Lint + custom lint rules (`:lint`),
  Konsist architecture tests (`:konsist`), Kover coverage gate, Aalekh, dependency-guard.
- Namespace root: `soft.divan.financemanager`.

## Build, test & verify commands

Run checks for the modules you touched; fix every violation your change introduces.

```bash
./gradlew :feature:<name>:impl:check    # full check for one module
./gradlew testDebugUnitTest             # all unit tests (Debug)
./gradlew :core:domain:testDebugUnitTest --tests "*GetTransactionsByPeriodUseCase*"  # single test class
./gradlew ktlintCheck                   # style   (./gradlew ktlintFormat to autofix)
./gradlew detekt                        # static analysis, maxIssues: 0, no baseline — fix, don't snapshot
./gradlew lint                          # Android lint + custom :lint checkers
./gradlew :konsist:test                 # architecture rules read from sources (see constraints below)
./gradlew :koverVerifyFull              # coverage gate: ≥ 95% lines of testable code
./gradlew :app:assertModuleGraph        # validate module dependency graph
./gradlew aalekhCheck                   # enforce architecture: layers, feature isolation, reachability, cycles (docs/aalekh.md)
./gradlew navCheck                      # navigation graph matches the committed .nav baselines
./gradlew navDump                       # refresh those baselines after an intentional nav change
./gradlew :app:dependencyGuard          # release classpath matches app/dependencies/releaseRuntimeClasspath.txt
./gradlew :app:dependencyGuardBaseline  # re-record it after an intentional dependency change
./gradlew app:assembleDebug            # build debug APK
```

CI (`.github/workflows/ci.yml`) runs, as separate jobs, on every PR and push to
`master`, and as the release gate inside `cd_release.yml` (push to `releases/**`; publishing
waits for it): `assembleDebug` (+ build-time report); unit tests (full `test` on master,
affected modules + `:konsist:test` on branches); coverage gate (`koverVerifyFull`); `lint`;
`detekt`; `ktlintCheck`; `:app:dependencyGuard`; `:app:assertModuleGraph`; `aalekhCheck`
(architecture); `navCheck` (job `nav-graph`); app size (`analyzeDebugBundle`).
`dependency-analysis` (`buildHealth`) is advisory and never blocks. A change touching only
`**/*.md` / `docs/**` skips them (job `changes`, `dorny/paths-filter`); the secret scan
(`security.yml`) always runs. A change that fails any blocking job will fail CI — run the
matching command locally before reporting done. Full pipeline description (incl. the CD
workflows, their secrets and the build-time report): `docs/ci-cd.md`.

## Big-picture architecture

Read these together to understand the system — the design lives across many files:

### Module layering
`app` + `core:*` + `feature:*` (each feature split into `:api` / `:impl`). Rules:
- `core:*` must **never** depend on `feature:*` or `app`.
- `feature:*:impl` depends on its own `:api`, `core:*`, and **other features' `:api` only —
  never another feature's `:impl`.
- `app` wires everything and owns the root navigation graph.
- Every module uses `soft.divan.*` convention plugins from `build-logic` and has its own `README.md`.
  New modules must be added to `settings.gradle.kts` and pass `:app:assertModuleGraph`.

Layers: **Presentation** = `feature:*:impl` (Compose + ViewModel mapping Domain→UiState);
**Domain** = `core:domain` (pure Kotlin: entities, UseCases, repository interfaces — no Android/Data deps);
**Data** = `core:data`/`core:database`/`core:network` (implements repos, DTO/Entity↔Domain mapping).

### Navigation is decoupled via `FeatureApi` (`core:feature-api`) — see `docs/navigation3.md`
Screens are addressed by `@Serializable` `NavKey`s declared in each feature's `:api` module
(arguments are fields of the key, never string routes). A feature exposes
`<Name>FeatureApi : FeatureApi` and implements
`registerEntries(scope: EntryProviderScope<NavKey>, navigator: Navigator, modifier)` in its
`:impl`, registering **only its own** keys — a key may be registered exactly once. Navigation
to another feature is `navigator.goTo(ItsKey)`; `navigator.back(from = ownKey)` closes the screen.
Both are idempotent (guard against double taps). `app` assembles the graph from a
Hilt `Set<FeatureApi>` multibinding (`di/FeatureNavigationModule.kt`) and owns two back stacks:
the root one (`RootNavDisplay`: splash → auth → main) and one per bottom-nav tab
(`TopLevelBackStack`). ViewModels that need nav arguments use Hilt assisted factories —
`SavedStateHandle` no longer carries them.

Because the graph is spread across features, it is also **described in annotations** and
extracted at build time by `compose-nav-graph` (see `docs/nav-graph.md`): a screen composable
carries `@NavDestination(route = XKey::class)`, one `@NavEdge(to = …)` per outgoing `goTo`,
and `@NavPreview` next to its `@Preview`. New screens/transitions MUST be annotated, and
`./gradlew navDump` re-records the committed `*/nav/*.nav` baselines.

### Error handling: `DomainResult` (see `docs/domain-result.md`)
The domain layer never throws across boundaries. Repositories/UseCases return
`DomainResult<T>` (`Success(data)` / `Failure(DomainError)`), often wrapped as
`Flow<DomainResult<T>>` for reactive streams. `DomainError` is a sealed hierarchy
(`NoData`, `Unauthorized`, `OperationNotAllowed`, `NetworkUnavailable`, `GuestModeBlocked`,
`Unknown(cause)`).
Consume with `fold` / `onSuccess` / `onFailure` / `getOrNull`; ViewModels map the result to `UiState`.

### Offline-first & sync
Room is the Single Source of Truth; UI observes Room via `Flow`. Network is only for
sync (`:sync` via WorkManager). Entity relations are **logical, not enforced** — no Room
`@ForeignKey`/`@Index`; transactions reference account/category by id resolved in code.
Split `localId`/`serverId`, soft deletes via `syncStatus = PENDING_DELETE`, and the
category → account → transaction sync order + last-write-wins are handled in the data layer.

## Non-obvious constraints

- **DB migrations:** pre-release the DB uses `fallbackToDestructiveMigration`. The `@Database`
  version MUST stay strictly greater than the prepackaged `category_db.db` asset's
  `user_version` (currently 1), or Room fails at runtime with an identity-hash mismatch.
  Adding an index = schema change = version bump. Real `Migration`s + migration tests are
  required before release (`exportSchema = true`).
- **Layer isolation:** never leak Room `Entity`, Retrofit `Response`/DTOs, or `HttpException`
  past the data layer. Map with `toDomain()` / `toEntity()` / `toDto()`. No business logic in UI.
- **Custom lint:** the `:lint` module enforces project rules — e.g. `OldDate` bans
  `java.util.Date`/`Calendar`. Don't introduce APIs the custom checkers reject.
- **Crypto:** use `core:security` `CryptoManager` (AES/GCM + KeyStore). Do **not** use the
  deprecated `androidx.security.crypto`. JWT is encrypted → DataStore (`core:auth`); PIN is
  hashed (PBKDF2 + salt) then the hash encrypted → SharedPreferences (`feature:security`).
  Never lift a raw secret/PIN into the presentation layer — expose only `verify(...)`.
- **Coroutines:** inject `CoroutineDispatcher`; never hardcode `Dispatchers.IO`/`Default` or
  use `GlobalScope`. Collect flows in UI with `collectAsStateWithLifecycle()`.
- **Dependencies:** any change to the release classpath (new library, version bump, changed
  transitive) fails `:app:dependencyGuard` until `./gradlew :app:dependencyGuardBaseline` re-records
  `app/dependencies/releaseRuntimeClasspath.txt` — review that diff, it ships with the change.
- **Coverage gate:** aggregate line coverage of testable code must stay ≥ 95% (`koverVerifyFull`,
  rule and exclusions in the root `build.gradle.kts`). UI (`@Composable`, `*ScreenKt`, Activities,
  `*FeatureImpl`) and generated code are excluded — so new non-UI logic ships with its tests.
- **Konsist** (`:konsist`) reads sources and fails on misplaced code: use cases and repository
  interfaces in a `domain` package (implementations outside it), Room entities only in
  `core:database`, DTOs in a `dto` package, ViewModels annotated `@HiltViewModel`, no legacy dates.
- **Building in a git worktree:** the gitignored `local.properties` and `app/google-services.json`
  are absent. Compile/test/lint tasks need `ANDROID_HOME=<sdk path>` before `./gradlew`
  (ktlint/detekt run without it). `:app` tasks need the google-services plugin set to
  `missingGoogleServicesStrategy = WARN` via an `--init-script` kept outside the repo — never copy
  the real `google-services.json`. Release/R8 builds locally: `-x :app:uploadCrashlyticsMappingFileRelease`.
- **Editing:** never mutate source with shell `sed`/`awk`/`echo >` — use the Edit/Write tools.

## Detailed guidelines (mandatory reading per area)

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
@./docs/agents/task-board.md

Deeper design docs live in `docs/`; their index, one line per doc, is `docs/README.md` — check it
before writing a new doc or guessing how a subsystem works.

## Interaction protocol

- The user is an experienced Android engineer. Reply in Russian. Skip basics and pleasantries —
  unless asked to explain something «с нуля»; then explain fully, in chat, not as a new doc.
- Verdict first, then reasoning. Give one recommendation, not a survey of options you won't pursue.
- Be direct: flag logical gaps, anti-patterns and missed edge cases — in the user's proposals
  too, not only in your own code. Disagree with a reason instead of complying silently or hedging.
- Before presenting a non-trivial design or change, self-review it by the three vectors in
  `docs/agents/release-process.md` («Self-review»).

## Clarify before executing

Do not start a non-trivial task on top of a guess. First scan the request for
ambiguity, then resolve it:

- **Ask the user** when the answer changes what you build and you can't settle it
  from the code, this file, or the linked docs — e.g. unclear scope or acceptance
  criteria, several valid architectural approaches with real trade-offs, which
  module/feature is meant, destructive or irreversible actions, or an assumed
  requirement that isn't stated. Ask up front, batch the questions, and propose a
  recommended default for each — don't drip one question at a time mid-task.
- **Decide yourself** for reversible, low-impact choices with an obvious
  convention (naming, file placement, which existing helper to reuse). State the
  assumption in your response and proceed — don't stall on things you can verify
  or that mirror existing patterns.
- **Never invent** missing facts (API shapes, field names, endpoints, business
  rules). Confirm from the codebase; if it isn't there and matters, ask.

Rule of thumb: if a wrong guess would waste real work or be hard to undo, ask;
otherwise pick the sensible default and note it.

## Workflow expectations

- **Never run git yourself** — do not commit, push, or open PRs; the user handles all git
  operations. Leave changes in the working tree. Once the code is done, only **write a short
  proposed commit message** (conventional-commit style, matching the existing Russian history,
  e.g. `fix(network): …`) for the user to copy — do not run `git commit`.
- **After the PR is opened, the task goes on:** before the report, run every blocking CI job
  locally; once the user opens the PR, turn on Auto-fix and triage CodeRabbit's comments each
  time it wakes the session — fix the valid ones, reject the rest with a reason, never comply
  silently. Loop and limits: `docs/agents/release-process.md` («After the PR is opened»).
- **No Claude/Anthropic attribution** in commits or generated text (no `Co-Authored-By`,
  no "Generated with Claude Code").
- **Task board:** work is tracked on the GitHub Projects board, a task's key is `FM-<issue>`.
  Propose the branch `feature/FM-N-<snake_case_name>` and end the commit subject with `(FM-N)`.
  Move the card to In Progress when you start; create new cards only after the user's "yes";
  never move cards to Done or close issues. Rules: `docs/agents/task-board.md`; commands: the
  `board` skill.
- **KDoc** required on new public classes/functions; every new UI component needs a `@Preview`
  (dark mode / font scale where relevant); update the module `README.md` when you change a module.
- **Design docs for complex features:** when a change introduces non-trivial technical design —
  a cross-module flow, a new architectural pattern, a client↔server contract, sync/offline logic,
  a state machine, or anything a reader can't grasp from a single file — write (or update) a
  Markdown doc under `docs/` and link it from `docs/README.md`. Match the existing docs' style
  (Russian, `#`/`##` sections, mermaid diagrams, code snippets, a "ключевые файлы" table). Keep it
  design-level (the "why" and how pieces fit), not a line-by-line restatement of the code. Skip it
  for small, local, or self-evident changes — KDoc/README is enough there.
- **Keep these instructions current — without asking.** When a task produces a durable decision
  (a new pattern or convention, a constraint discovered the hard way, a tooling gotcha), add it in
  the same change: area rules to the matching `docs/agents/*.md`, cross-cutting ones here. Fix or
  delete a rule that turned out wrong. Don't record task status, one-off facts or what the code
  already states. Never relax process or safety rules (git, the board, attribution, secrets) on
  your own — that takes the user's explicit request. Mention every such edit in the report.
- Prefer the smallest change that satisfies the requirement; mirror existing style and patterns.
- Don't rename public APIs or restructure packages without an explicit request.
- When done, give a short report: what changed, architecture/module-graph impact, tests added,
  self-review (faults / edge cases / improvements), instruction updates, and the task card
  (`FM-N <title>`, its status, proposed branch `feature/FM-N-…`, commit message ending with
  `(FM-N)`, follow-up cards to create).
