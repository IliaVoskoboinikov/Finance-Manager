# `:feature:languages:impl`

## Responsibility

Обработка и отображение языковых настроек приложения.

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
        subgraph :feature:languages
            :feature:languages:api
            :feature:languages:impl
        end
    end

    subgraph :core
        :core:uikit
        :core:feature-api
    end

    :feature:languages:impl --> :feature:languages:api
    :feature:languages:impl --> :core:uikit
    :feature:languages:api --> :core:feature-api

    classDef android-library fill:#9BF6FF,stroke:#000,stroke-width:2px,color:#000;
```// Revue me>>
