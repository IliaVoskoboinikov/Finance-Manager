# `:feature:synchronization:impl`

## Responsibility

Настройка синхронизации данных между локальным хранилищем и сервером.

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
        subgraph :feature:synchronization
            :feature:synchronization:api
            :feature:synchronization:impl
        end
    end

    subgraph :core
        :core:uikit
        :core:domain
        :core:feature-api
    end

    :feature:synchronization:impl --> :feature:synchronization:api
    :feature:synchronization:impl --> :core:uikit
    :feature:synchronization:impl --> :core:domain
    :feature:synchronization:impl --> :sync
    :feature:synchronization:api --> :core:feature-api

    classDef android-library fill:#9BF6FF,stroke:#000,stroke-width:2px,color:#000;
```