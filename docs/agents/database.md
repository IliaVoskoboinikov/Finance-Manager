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
*   Do **not** launch un-rollbackable side effects (network sync) *inside* a DB transaction —
    a rollback won't undo them. Use `AppCoroutineContext.launchSync`: inside `runInTransaction`
    it defers the action until a successful commit (and drops it on rollback), outside it runs
    immediately. Mechanism and caveats: [docs/post-commit-sync.md](../post-commit-sync.md).

## Migrations
*   For production, schema changes REQUIRE a `Migration` plus a migration test
    (`MigrationTestHelper`); `exportSchema = true` is enabled for this.
*   **Current pre-release state:** the DB uses `fallbackToDestructiveMigration`. The `@Database`
    version MUST stay strictly greater than the prepackaged `category_db.db` asset's `user_version`
    (currently 1) — with an equal version but a different schema, Room fails at runtime with an
    identity-hash mismatch. This destructive setup is acceptable only while there are no real users
    (offline-first, data re-syncs from the server) and MUST be replaced with real migrations before
    release.

## Data Isolation
*   Room `Entity` classes are internal to the data layer.
*   Map Entities to Domain models in the Repository (`toDomain()` / `toEntity()`).
