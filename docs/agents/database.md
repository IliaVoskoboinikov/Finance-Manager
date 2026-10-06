# Database & Persistence

## Room Best Practices
*   **SSOT:** The database is the Single Source of Truth. UI should observe data from Room.
*   **DAOs:** Return `Flow<T>` for reactive updates. Use `suspend` for one-shot operations.

## Access Through `DatabaseHolder`
*   The database is encrypted (SQLCipher) and can be **closed** (PIN level, auto-lock, wipe), so
    DAOs and `FinanceManagerDatabase` are **not** injectable. Data sources get the database from
    `DatabaseHolder` on every call:
    *   one-shot: `holder.withDatabase { it.accountDao().getByLocalId(id) }` — holds a lease so the
        database is not closed mid-operation; throws `DatabaseLockedException` when closed;
    *   reactive: `holder.observe { it.accountDao().getAll() }` — silent while closed,
        re-subscribes to the new instance after reopening.
*   Never return a Room `Flow` from inside `withDatabase` — it would stay bound to the instance
    that was current at the call; use `observe`.
*   Do not open/close/wipe the database from inside `withDatabase` (it would wait for itself).
    Design: [docs/encryption.md](../encryption.md).

## Relations & Indices
*   Entity relations are **logical**, not enforced: transactions reference a account/category
    via `accountLocalId` / `categoryId` (resolved in code), and Room `@ForeignKey` / `@Index`
    are intentionally **not** used. This fits the offline-first model (split `localId`/`serverId`,
    soft deletes via `syncStatus = PENDING_DELETE`, and the category → account → transaction sync
    order + last-write-wins, all handled in the data layer).
*   Indices (e.g. `transactions(accountLocalId, transactionDate)`, `transactions(serverId)`) may be
    added as an optimization when data grows — this changes the schema and REQUIRES a version bump.

## Transactions
*   Use `@Transaction` (DAOs) / `withTransaction` (`RoomTransactionRunner`) for multi-step atomic
    operations, e.g. updating an account balance together with a transaction.
*   Do **not** launch un-rollbackable side effects (network sync) *inside* a DB transaction —
    a rollback won't undo them. Use `AppCoroutineContext.launchSync`: inside `runInTransaction`
    it defers the action until a successful commit (and drops it on rollback), outside it runs
    immediately. Mechanism and caveats: [docs/post-commit-sync.md](../post-commit-sync.md).

## Migrations
*   Every schema change (including adding an index) REQUIRES a version bump, a
    `Migration(n, n + 1)` in `DatabaseMigrations.ALL` and a test in `MigrationTest`
    (`MigrationTestHelper`). The base schema is **v8**; schemas are exported by the Room Gradle
    plugin to `core/database/schemas` — commit the new `<version>.json`.
*   There is no destructive fallback on upgrade: a missing migration fails on open (and in tests)
    instead of silently dropping user data. Only a downgrade recreates the database
    (developer branch switching).
*   Default categories are seeded in code (`SeedDatabaseCallback`, `DefaultCategories`) on
    database creation — there is no prepackaged asset anymore (`createFromAsset` cannot be
    encrypted). Every builder (production and tests) uses `withAppDefaults()`.

## Data Isolation
*   Room `Entity` classes are internal to the data layer.
*   Map Entities to Domain models in the Repository (`toDomain()` / `toEntity()`).
