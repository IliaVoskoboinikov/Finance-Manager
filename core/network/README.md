# `:core:network`

## Responsibility

Сетевой слой приложения.

HTTP-кеш OkHttp для API **выключен**: ответы с суммами лежали бы в `cacheDir` открытым текстом,
а данные и так живут в зашифрованной базе. Каталог кеша прежних версий удаляет при старте
`LegacyHttpCache`.

Суммы (`BigDecimal`) разбирает `BigDecimalTypeAdapter`: без `Double`, а неразборчивое значение
даёт `JsonSyntaxException` с путём поля, без самого значения. На Android 8–11 текст ошибки
`BigDecimal(String)` содержит всё число, а ошибки разбора доходят до логов.

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
    :core:network --> :core:auth
    classDef android-library fill: #9BF6FF, stroke: #000, stroke-width: 2px, color: #000;
```
