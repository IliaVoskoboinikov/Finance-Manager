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

- **Kotlin `2.4.0`, Java 11**, AGP `9.2.1`, `compileSdk 36`, `minSdk 26`.
- **UI:** Jetpack Compose + Material 3 (custom components in `core:uikit`).
- **Architecture:** Clean Architecture + MVVM + Unidirectional Data Flow (UDF), offline-first.
- **DI:** Dagger Hilt `2.59.2`. **Async:** Coroutines + Flow.
- **Storage:** Room `2.8.4` (`FinanceManagerDatabase`, SSOT) + DataStore.
- **Network:** Retrofit + OkHttp + Gson; custom interceptors (Auth, Retry, NetworkConnection, Logging).
- **Background:** WorkManager (`:sync`). **Security:** `core:security` `CryptoManager` (AES/GCM + KeyStore).
- **Quality:** Detekt, ktlint, Android Lint + custom lint rules (`:lint`).
- Namespace root: `soft.divan.financemanager`.

## Build, test & verify commands

Run checks for the modules you touched; fix every violation your change introduces.

```bash
./gradlew :feature:<name>:impl:check    # full check for one module
./gradlew testDebugUnitTest             # all unit tests (Debug)
./gradlew :core:domain:testDebugUnitTest --tests "*GetTransactionsByPeriodUseCase*"  # single test class
./gradlew ktlintCheck                   # style   (./gradlew ktlintFormat to autofix)
./gradlew detekt                        # static analysis (./gradlew detektBaseline to snapshot)
./gradlew lint                          # Android lint + custom :lint checkers
./gradlew :app:assertModuleGraph        # validate module dependency graph
./gradlew app:assembleDebug            # build debug APK
```

CI (`.github/workflows/ci.yml`) runs, as separate jobs, on every non-`.md` push:
`assembleDebug`, `testDebugUnitTest`, `lint`, `detekt`, `ktlintCheck`,
`:app:assertModuleGraph`, app-size (`analyzeDebugBundle`), and build-time report.
A change that fails any of these will fail CI — run the matching command locally
before reporting done.

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

### Navigation is decoupled via `FeatureApi` (`core:feature-api`)
Each feature exposes a `<Name>FeatureApi : FeatureApi` interface from its `:api` module,
declaring its `route` and a `registerGraph(navGraphBuilder, navController, scope, modifier)`.
The `:impl` implements it; `app` (`presenter/navigation/RootNavGraph.kt`, `BottomNavGraph.kt`)
injects the `FeatureApi` instances via Hilt and calls `registerGraph` to assemble the graph —
so features never reference each other's screens directly. Routes are built with the typed
`RouteScope` helper, not string concatenation.

### Error handling: `DomainResult` (see `docs/domain-result.md`)
The domain layer never throws across boundaries. Repositories/UseCases return
`DomainResult<T>` (`Success(data)` / `Failure(DomainError)`), often wrapped as
`Flow<DomainResult<T>>` for reactive streams. `DomainError` is a sealed hierarchy
(`NoData`, `Unauthorized`, `OperationNotAllowed`, `NetworkUnavailable`, `Unknown(cause)`).
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

Deeper design docs live in `docs/` (`architecture.md`, `modularization.md`, `modules.md`,
`auth.md`, `synchronization.md`, `domain-result.md`, `bd.md`).

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
- **No Claude/Anthropic attribution** in commits or generated text (no `Co-Authored-By`,
  no "Generated with Claude Code").
- **KDoc** required on new public classes/functions; every new UI component needs a `@Preview`
  (dark mode / font scale where relevant); update the module `README.md` when you change a module.
- Prefer the smallest change that satisfies the requirement; mirror existing style and patterns.
- Don't rename public APIs or restructure packages without an explicit request.
- When done, give a short report: what changed, architecture/module-graph impact, tests added, risks.
