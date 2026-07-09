# `:core:data`

## Responsibility

Слой управления данными приложения: реализации репозиториев (`core:domain`), локальные и
удалённые data sources, маппинг Entity/DTO ↔ Domain, sync-менеджеры (pull/push,
last-write-wins) и транзакционный механизм.

## Transactions & post-commit sync

Составные локальные операции выполняются атомарно через `TransactionRunner`
(`RoomTransactionRunner` + `rollbackOnError()`). Сетевые пуши, инициированные внутри
транзакции, откладываются до успешного commit и отбрасываются при rollback —
см. [docs/post-commit-sync.md](../../docs/post-commit-sync.md). В suspend-методах
репозиториев для запуска фонового синка после записи используйте
`AppCoroutineContext.launchSync`, а не `launch`.

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
    :core:data --> :core:logging-error
    :core:data --> :core:database
    :core:data --> :core:domain
    :core:data --> :core:network
    classDef android-library fill: #9BF6FF, stroke: #000, stroke-width: 2px, color: #000;
```
