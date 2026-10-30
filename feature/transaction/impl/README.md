# `:feature:transaction:impl`

## Responsibility

Обработка и отображение транзакции пользователя.

Сохранение и удаление выполняются по одному за раз, а после успеха экран больше не принимает
действий: двойной тап по ✓ не создаст операцию дважды и не изменит баланс счёта второй раз.

Если новую операцию не из чего собрать (нет счёта или категорий нужного типа), экран показывает
пустое состояние: без счёта — кнопку «Создать счёт» (переход на `AccountKey` из
`:feature:account:api`), без категорий — пояснение и «Закрыть». Пока экран открыт, он слушает БД
и сам откроет форму, когда данные появятся; после этого форма уже не пересобирается и ввод не
теряется.

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
        subgraph :feature:transaction
            :feature:transaction:api
            :feature:transaction:impl
        end

        :feature:account:api
        :feature:haptics:api
        :feature:sounds:api
    end

    subgraph :core
        :core:data
        :core:domain
        :core:uikit
        :core:feature-api
    end

    :feature:transaction:impl --> :feature:transaction:api
    :feature:transaction:impl --> :core:domain
    :feature:transaction:impl --> :core:data
    :feature:transaction:impl --> :core:uikit
    :feature:transaction:impl --> :feature:account:api
    :feature:transaction:impl --> :feature:haptics:api
    :feature:transaction:impl --> :feature:sounds:api
    :feature:transaction:api --> :core:feature-api

    classDef android-library fill:#9BF6FF,stroke:#000,stroke-width:2px,color:#000;
```