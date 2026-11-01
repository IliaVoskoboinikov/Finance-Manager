# `:core:database`

## Responsibility

Слой работы с базой данных приложения.

Справочник категорий (`seed/DefaultCategories`, id — серверные GUID) засевается кодом:
`SeedDatabaseCallback` кладёт его при создании базы и после деструктивной миграции, поэтому
категории есть с первого запуска, в том числе у гостя без сети.

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
    :core:database 

    classDef android-library fill:#9BF6FF,stroke:#000,stroke-width:2px,color:#000;
```