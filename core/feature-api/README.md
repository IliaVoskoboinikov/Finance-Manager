# `:core:feature-api`

## Responsibility

Контракт навигации между фичами:

- `FeatureApi` — интерфейс, который реализует каждая фича (`route` + `registerGraph(...)`);
  модуль `app` внедряет реализации через Hilt и собирает из них корневой навигационный граф.
- `RouteScope` — типобезопасный билдер маршрутов вместо ручной конкатенации строк.
- `NavExt` — навигационные расширения.

От этого модуля зависят все `:feature:*:api`.

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
    :core:feature-api

    classDef android-library fill:#9BF6FF,stroke:#000,stroke-width:2px,color:#000;
```
