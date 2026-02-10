# `:feature:transactions-today:api`

## Responsibility

Предоставление API для работы с транзакциями за текущий день.

## Module dependency graph

<!--region graph-->

```mermaid
---
config:
  layout: elk
  elk:
    nodePlacementStrategy: SIMPLE
---
graph TB
    :feature:transactions-today:api --> :core:feature-api

    classDef android-library fill:#9BF6FF,stroke:#000,stroke-width:2px,color:#000;
```// Revue me>>
