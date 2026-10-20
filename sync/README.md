# `:sync`

## Responsibility

Служба синхронизации данных приложения.

Строки foreground-уведомления синка (имя и описание канала, заголовок) лежат в ресурсах модуля
(`res/values`, `res/values-ru`).

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
    :sync --> :core:workmanager

    classDef android-library fill:#9BF6FF,stroke:#000,stroke-width:2px,color:#000;
```
