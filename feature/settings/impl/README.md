# `:feature:settings:impl`

## Responsibility

Реализация экрана и логики настроек приложения.

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
        direction TB
        subgraph :feature:settings
            :feature:settings:api[api]:::android-library
            :feature:settings:impl[impl]:::android-library
        end

        :feature:security:api
        :feature:design-app:api
        :feature:haptics:api
        :feature:sounds:api
        :feature:languages:api
        :feature:synchronization:api
    end

    subgraph :core
        :core:uikit
        :core:feature-api
    end

    :feature:settings:impl --> :feature:settings:api
    :feature:settings:impl --> :core:uikit
    :feature:settings:impl --> :feature:security:api
    :feature:settings:impl --> :feature:design-app:api
    :feature:settings:impl --> :feature:haptics:api
    :feature:settings:impl --> :feature:sounds:api
    :feature:settings:impl --> :feature:languages:api
    :feature:settings:impl --> :feature:synchronization:api
    :feature:settings:api --> :core:feature-api
    classDef android-library fill: #9BF6FF, stroke: #000, stroke-width: 2px, color: #000;
```// Revue me>>
