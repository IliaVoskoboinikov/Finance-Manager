# `:core:logging-error`

## Responsibility

Логирование нефатальных ошибок: интерфейс `ErrorLogger` и его реализация
`CrashlyticsLogger` (Firebase Crashlytics, подключается через `CrashlyticsModule`).

В сообщения нельзя передавать PII, credentials и токены (см. `docs/agents/security.md`).

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
    :core:logging-error

    classDef android-library fill:#9BF6FF,stroke:#000,stroke-width:2px,color:#000;
```
