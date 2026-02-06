# `:core:data`

## Responsibility

Слой управления данными приложения.

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
    :core:data --> :core:common
    :core:data --> :core:logging-error:api
    :core:data --> :core:database
    :core:data --> :core:domain
    :core:data --> :core:network
    classDef android-library fill: #9BF6FF, stroke: #000, stroke-width: 2px, color: #000;
```// Revue me>>
