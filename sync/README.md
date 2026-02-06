# `:sync`

## Responsibility

Служба синхронизации данных приложения.

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
    :sync --> :core:data
    :sync --> :core:common

    classDef android-library fill:#9BF6FF,stroke:#000,stroke-width:2px,color:#000;
