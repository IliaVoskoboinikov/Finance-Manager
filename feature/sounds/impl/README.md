# `:feature:sounds:impl`

## Responsibility

Обеспечение звуковой обратной связи в приложении.

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
        subgraph :feature:sounds
            :feature:sounds:api
            :feature:sounds:impl
        end
    end

    subgraph :core
        :core:uikit
        :core:common
        :core:feature-api
    end

    :feature:sounds:impl --> :feature:sounds:api
    :feature:sounds:impl --> :core:common
    :feature:sounds:impl --> :core:uikit
    :feature:sounds:api --> :core:feature-api
    classDef android-library fill: #9BF6FF, stroke: #000, stroke-width: 2px, color: #000;
```// Revue me>>
