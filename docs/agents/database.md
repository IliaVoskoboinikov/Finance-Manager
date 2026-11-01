# Database & Persistence

## Room Best Practices
*   **SSOT:** The database is the Single Source of Truth. UI should observe data from Room.
*   **DAOs:** Return `Flow<T>` for reactive updates. Use `suspend` for one-shot operations.

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
*   Do **not** launch un-rollbackable side effects (network sync via `appCoroutineContext.launch`)
    *inside* a DB transaction — a rollback won't undo them. Enqueue such work after a successful
    commit.

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
    have to track `@Database` on every bump.
*   Room calls `onDestructiveMigration` after dropping the tables but **before** creating them
    again — writing there fails with `no such table`. The callback only sets a flag and seeds in
    `onOpen`.

## Data Isolation
*   Room `Entity` classes are internal to the data layer.
*   Map Entities to Domain models in the Repository (`toDomain()` / `toEntity()`).
