# `:core:domain`

## Responsibility

Бизнес-логика и модели предметной области приложения.

Доступ к локальным данным описан без Android и без ключей: `LocalDataState` (открыты, заперты
PIN-кодом, ключ утерян), `DataProtectionLevel`, результаты разблокировки и смены защиты,
репозитории `LocalDataAccessRepository` и `DataProtectionRepository`. Реализации — в
`:core:data`, дизайн — [docs/encryption.md](../../docs/encryption.md).

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
    :core:domain

    classDef android-library fill:#9BF6FF,stroke:#000,stroke-width:2px,color:#000;
```