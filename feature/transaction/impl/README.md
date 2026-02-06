# `:feature:transaction:impl`

## Responsibility

Обработка и отображение транзакции пользователя.

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
    subgraph :feature
        subgraph :feature:transaction
            :feature:transaction:api
            :feature:transaction:impl
        end

        :feature:haptics:api
        :feature:sounds:api
    end

    subgraph :core
        :core:data
        :core:domain
        :core:uikit
        :core:feature-api
    end

    :feature:transaction:impl --> :feature:transaction:api
    :feature:transaction:impl --> :core:domain
    :feature:transaction:impl --> :core:data
    :feature:transaction:impl --> :core:uikit
    :feature:transaction:impl --> :feature:haptics:api
    :feature:transaction:impl --> :feature:sounds:api
    :feature:transaction:api --> :core:feature-api

    classDef android-library fill:#9BF6FF,stroke:#000,stroke-width:2px,color:#000;
```