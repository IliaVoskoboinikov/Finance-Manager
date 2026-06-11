# Navigation

## Navigation Compose
*   Use Navigation Compose for all screen transitions.
*   Routes must be centralized.

## Feature Entry Points
*   Feature modules expose their destinations through their `:api` module.
*   The `app` module assembles the root navigation graph.

## Type Safety
*   Navigation arguments must be strongly typed.
*   Avoid manual string concatenation for building routes with arguments.
*   Use the navigation library's support for typed destinations where possible.

## Ownership
*   A feature's navigation logic resides in its `:impl` module, but its "contract" (route definitions) resides in `:api`.
