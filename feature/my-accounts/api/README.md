# `:feature:my-accounts:api`

## Responsibility

Предоставление API для работы с аккаунтами пользователя.

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
    :feature:my-accounts:api --> :core:feature-api

    classDef android-library fill:#9BF6FF,stroke:#000,stroke-width:2px,color:#000;
```// Revue me>>
