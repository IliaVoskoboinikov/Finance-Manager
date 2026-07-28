# `:feature:analysis:impl`

## Responsibility

Аналитика транзакций и графиков.

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
        subgraph :feature:analysis
            :feature:analysis:api
            :feature:analysis:impl
        end
    end

    subgraph :core
        :core:common
        :core:uikit
        :core:domain
        :core:feature-api
    end

    :feature:analysis:impl --> :feature:analysis:api
    :feature:analysis:impl --> :core:common
    :feature:analysis:impl --> :core:uikit
    :feature:analysis:impl --> :core:domain
    :feature:analysis:api --> :core:feature-api

    classDef android-library fill:#9BF6FF,stroke:#000,stroke-width:2px,color:#000;
```
