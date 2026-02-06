# `:feature:my-accounts:impl`

## Responsibility

Обработка и отображение данных пользовательских счетов.

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
        subgraph :feature:my-accounts
            :feature:my-accounts:api
            :feature:my-accounts:impl
        end

        :feature:account:api
        :feature:haptics:api
    end

    subgraph :core
        :core:data
        :core:domain
        :core:uikit
        :core:network
        :core:feature-api
    end

    :feature:my-accounts:impl --> :feature:my-accounts:api
    :feature:my-accounts:impl --> :core:network
    :feature:my-accounts:impl --> :core:uikit
    :feature:my-accounts:impl --> :core:domain
    :feature:my-accounts:impl --> :core:data
    :feature:my-accounts:impl --> :feature:account:api
    :feature:my-accounts:impl --> :feature:haptics:api

    :feature:my-accounts:api --> :core:feature-api

    classDef android-library fill:#9BF6FF,stroke:#000,stroke-width:2px,color:#000;
```
