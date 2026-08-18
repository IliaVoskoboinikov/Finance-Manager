# `:core:workmanager`

## Responsibility

Общая инфраструктура WorkManager для библиотечных модулей: `DelegatingWorker`,
`HiltWorkerFactoryEntryPoint` и расширение `KClass<out CoroutineWorker>.delegatedData()`.

`:app` не переопределяет инициализацию WorkManager (`Configuration.Provider` отсутствует),
поэтому системная фабрика умеет создавать только воркеры с конструктором
`(Context, WorkerParameters)` и не может собрать `@HiltWorker` с зависимостями.
`DelegatingWorker` закрывает этот разрыв: в очередь ставится он, а имя настоящего воркера
едет в `inputData` и разрешается через `HiltWorkerFactory`.

Модуль выделен из `:sync`, когда второй потребитель (`:core:notifications`) тоже
захотел планировать `@HiltWorker`: зависимость `core:* → :sync` нарушила бы слоистость,
поэтому общий примитив вынесен в `core`.

Зависимости `androidx.work` и `hilt-work` объявлены как `api` — они часть публичного
контракта модуля.

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
    :core:workmanager

    classDef android-library fill:#9BF6FF,stroke:#000,stroke-width:2px,color:#000;
```
