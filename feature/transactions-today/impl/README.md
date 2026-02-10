# `:feature:transactions-today:impl`

## Responsibility

Отображение и обработка транзакций пользователя за текущий день.

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
        subgraph :feature:transactions-today
            :feature:transactions-today:api
            :feature:transactions-today:impl
        end

        :feature:transaction:api
        :feature:history:api
        :feature:haptics:api
    end

    subgraph :core
        :core:network
        :core:uikit
        :core:domain
        :core:feature-api
    end

    :feature:transactions-today:impl --> :feature:transactions-today:api
    :feature:transactions-today:impl --> :core:network
    :feature:transactions-today:impl --> :core:uikit
    :feature:transactions-today:impl --> :core:domain
    :feature:transactions-today:impl --> :feature:transaction:api
    :feature:transactions-today:impl --> :feature:history:api
    :feature:transactions-today:impl --> :feature:haptics:api
    :feature:transactions-today:api --> :core:feature-api
    classDef android-library fill: #9BF6FF, stroke: #000, stroke-width: 2px, color: #000;

```// Revue me>>
