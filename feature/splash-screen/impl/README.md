# `:feature:splash-screen:impl`

## Responsibility

Отображение и обработка стартового экрана приложения.

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
        subgraph :feature:splash-screen
            :feature:splash-screen:api
            :feature:splash-screen:impl
        end
    end

    subgraph :core
        :core:uikit
        :core:feature-api
    end

    :feature:splash-screen:impl --> :feature:splash-screen:api
    :feature:splash-screen:impl --> :core:uikit
    :feature:splash-screen:api --> :core:feature-api

    classDef android-library fill:#9BF6FF,stroke:#000,stroke-width:2px,color:#000;
```