# `:feature:sounds:impl`

## Responsibility

Обеспечение звуковой обратной связи в приложении.

`SoundsPoolHolder` играет звуки как sonification (`USAGE_ASSISTANCE_SONIFICATION` →
`STREAM_SYSTEM`): они глушатся беззвучным и вибро-режимом и не зависят от громкости медиа.
Запрос на звук, который `SoundPool` ещё загружает, откладывается и выполняется после загрузки,
поэтому первый звук после старта не теряется.

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
        subgraph :feature:sounds
            :feature:sounds:api
            :feature:sounds:impl
        end
    end

    subgraph :core
        :core:uikit
        :core:common
        :core:feature-api
    end

    :feature:sounds:impl --> :feature:sounds:api
    :feature:sounds:impl --> :core:common
    :feature:sounds:impl --> :core:uikit
    :feature:sounds:api --> :core:feature-api
    classDef android-library fill: #9BF6FF, stroke: #000, stroke-width: 2px, color: #000;
```