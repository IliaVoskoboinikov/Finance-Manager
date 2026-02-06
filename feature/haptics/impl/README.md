# `:feature:haptics:impl`

## Responsibility

Обеспечение тактильной обратной связи в приложении.

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
        subgraph :feature:haptics
            :feature:haptics:api
            :feature:haptics:impl
        end
    end

    subgraph :core
        :core:uikit
        :core:common
        :core:feature-api
    end

    :feature:haptics:impl --> :feature:haptics:api
    :feature:haptics:impl --> :core:uikit
    :feature:haptics:impl --> :core:common
    :feature:haptics:api --> :core:feature-api
    classDef android-library fill: #9BF6FF, stroke: #000, stroke-width: 2px, color: #000;
```// Revue me>>
