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
*   `safeDbCall` inside `runInTransaction` swallows the exception, and only an exception rolls the
    transaction back — so end it with `.rollbackOnError()`. Otherwise a failing second write
    (e.g. the outbox `enqueue`) commits the first one alone.

## Queries
*   A `List` parameter expands into one bind variable per element, and the platform SQLite on
    API 26–30 allows at most 999. Split long id lists before the DAO call (`queryInChunks`,
    `SYNC_IDS_CHUNK_SIZE`), and remember a list used twice in one query costs double. Never treat
    a failed lookup as "nothing found locally" — in a pull that inserts duplicates.

## Migrations
*   For production, schema changes REQUIRE a `Migration` plus a migration test
    (`MigrationTestHelper`); `exportSchema = true` is enabled for this.
*   **Current pre-release state:** the DB uses `fallbackToDestructiveMigration`. This destructive
    setup is acceptable only while there are no real users (offline-first, data re-syncs from the
    server) and MUST be replaced with real migrations before release.

## Seed data
*   Reference data the app needs offline (the 24 categories) is seeded in code:
    `seed/DefaultCategories` + `SeedDatabaseCallback`, ids equal to the backend's GUIDs so the
    server pull `REPLACE`s the same rows instead of adding duplicates, and guest transactions
    merged on sign-in point to categories the server knows.
*   Don't go back to `createFromAsset`: an asset older than the schema is dropped by the
    destructive migration (fresh installs had zero categories, FM-128), and its version would
    have to track `@Database` on every bump. An asset is also a plain SQLite file, and the
    encrypted database cannot be created from it.
*   The production builder (SQLCipher) and test databases that stand in for it
    (`TestDatabaseFactory`, `VaultHarness`, `DefaultCategoriesTest`) go through `withAppDefaults()`
    (`core:database` `db/DatabaseDefaults.kt`): the seed callback and the destructive fallback live
    there. Plain in-memory databases for DAO and outbox tests are built without it — they rely on
    empty tables.
*   Room calls `onDestructiveMigration` after dropping the tables but **before** creating them
    again — writing there fails with `no such table`. The callback only sets a flag and seeds in
    `onOpen`.

## Data Isolation
*   Room `Entity` classes are internal to the data layer.
*   Map Entities to Domain models in the Repository (`toDomain()` / `toEntity()`).
