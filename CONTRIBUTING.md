# Contributing to Finance Manager

Thanks for your interest! Finance Manager is a one-maintainer pet project, so the process is light,
but every change goes through the same CI gates as the maintainer's own work.

- **Questions and ideas** → [Discussions](https://github.com/IliaVoskoboinikov/Finance-Manager/discussions).
- **Bugs and feature requests** → [issues](https://github.com/IliaVoskoboinikov/Finance-Manager/issues/new/choose)
  (use a template).
- **Security problems** → never in public, see [SECURITY.md](SECURITY.md).

By participating you agree to follow the [Code of Conduct](CODE_OF_CONDUCT.md).

## Language

Documentation in [`docs/`](docs/README.md), issues, KDoc and commit messages are in **Russian** —
that is the project's working language. English issues and pull requests are welcome too.

## Build

Requirements: JDK 21 and a recent stable Android Studio (or the Android SDK with `ANDROID_HOME` set).

```bash
git clone https://github.com/IliaVoskoboinikov/Finance-Manager.git
cd Finance-Manager
./gradlew :app:installDebug
```

No secrets are needed for a debug build. Optional values go to `local.properties` (gitignored):

| Key / file | Without it |
|------------|------------|
| `app/google-services.json` | no Crashlytics and no push notifications (the build prints a warning) |
| `YANDEX_CLIENT_ID` | Yandex ID sign-in does not work; guest mode works |
| `API_TOKEN` | not used by the app at the moment |

More commands (tests, coverage, reports, ADB recipes): [docs/commands.md](docs/commands.md).

## Before you open a pull request

1. Pick an issue from the [task board](https://github.com/users/IliaVoskoboinikov/projects/5) or open
   one first for anything bigger than a small fix, so we agree on the approach.
2. Branch name: `feature/FM-<issue number>-<snake_case_name>`, e.g. `feature/FM-42-fix_navigation`.
3. Commit messages and the PR title follow [Conventional Commits](https://www.conventionalcommits.org/):
   `type(scope): description (FM-42)`, at most 72 characters.
4. The PR body contains `Closes #42`.
5. Run the checks CI will run on your change:

   ```bash
   ./gradlew assembleDebug testDebugUnitTest :core:domain:test :konsist:test :lint:test
   ./gradlew ktlintCheck detekt lint
   ./gradlew :koverVerifyFull                # line coverage of testable code ≥ 70%
   ./gradlew :app:dependencyGuard :app:assertModuleGraph aalekhCheck navCheck
   ```

   The full list of blocking jobs and what each one checks: [docs/ci-cd.md](docs/ci-cd.md).

## Project rules in short

The architecture is enforced by tooling, so most rules fail the build rather than the review:

- `feature:*:impl` depends only on its own `:api`, `core:*` and other features' `:api` — never on another
  `:impl` (`assertModuleGraph`, Aalekh).
- The domain layer (`core:domain`) is pure Kotlin and returns `DomainResult`, it never throws across
  layers ([docs/domain-result.md](docs/domain-result.md)).
- Room is the single source of truth; DTOs and entities never leave the data layer (Konsist).
- New screens and transitions are annotated for the navigation map (`@NavDestination`, `@NavEdge`,
  `@NavPreview`) and `./gradlew navDump` re-records the baselines ([docs/nav-graph.md](docs/nav-graph.md)).
- New non-UI logic ships with unit tests; new UI components get a `@Preview`.
- Library versions live only in `gradle/libs.versions.toml`; a new dependency means
  `./gradlew :app:dependencyGuardBaseline` and a reviewed diff.

The complete, area-by-area rule set is in [`docs/agents/`](docs/agents/) — it is written for AI coding
agents, but it is the most precise description of the conventions for humans too.

## License

By contributing, you agree that your contributions are licensed under the [Apache License 2.0](LICENSE).
