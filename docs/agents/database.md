# Database & Persistence

## Room Best Practices
*   **SSOT:** The database is the Single Source of Truth. UI should observe data from Room.
*   **DAOs:** Return `Flow<T>` for reactive updates. Use `suspend` for one-shot operations.

## Transactions
*   Use `@Transaction` for DAOs that perform multiple operations to ensure atomicity.

## Migrations
*   Schema changes REQUIRE a migration.
*   **Never** use `fallbackToDestructiveMigration` in production code.
*   Include a test case for every new migration.

## Data Isolation
*   Room `Entity` classes are internal to the data layer.
*   Map Entities to Domain models in the Repository.
