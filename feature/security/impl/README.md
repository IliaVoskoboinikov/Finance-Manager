# `:feature:security:impl`

## Responsibility

Обеспечение безопасности приложения и пользовательских данных.

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
        subgraph :feature:security
            :feature:security:api
            :feature:security:impl
        end
    end

    subgraph :core
        :core:network
        :core:uikit
        :core:domain
        :core:feature-api
    end

    :feature:security:impl --> :feature:security:api
    :feature:security:impl --> :core:network
    :feature:security:impl --> :core:uikit
    :feature:security:impl --> :core:domain
    :feature:security:api --> :core:feature-api

    classDef android-library fill:#9BF6FF,stroke:#000,stroke-width:2px,color:#000;
```