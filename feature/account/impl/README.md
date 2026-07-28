# `:feature:account:impl`

## Responsibility

Создание и редактирование счёта пользователя: экран `AccountScreen` и `AccountViewModel`
(режимы `AccountMode.Create` / `AccountMode.Edit` — имя, баланс, валюта), а также удаление
счёта с экрана редактирования.

Use case-ы модуля: `CreateAccountUseCase`, `UpdateAccountUseCase`, `GetAccountByIdUseCase`,
`DeleteAccountUseCase`. Удаление — мягкое (offline-first): в data-слое счёт помечается
`syncStatus = PENDING_DELETE` и не удаляется, пока по нему есть транзакции
(проверка в `AccountRepositoryImpl.delete`).

Модуль реализует `AccountFeatureApi` из `:feature:account:api` и регистрирует свой
навигационный граф в `app`.

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
        subgraph :feature:account
            :feature:account:api
            :feature:account:impl
        end

        :feature:haptics:api
    end

    subgraph :core
        :core:uikit
        :core:domain
        :core:feature-api
    end

    :feature:account:impl --> :feature:account:api
    :feature:account:impl --> :core:domain
    :feature:account:impl --> :core:uikit
    :feature:account:impl --> :feature:haptics:api
    :feature:account:api --> :core:feature-api

    classDef android-library fill:#9BF6FF,stroke:#000,stroke-width:2px,color:#000;
```
