# `:feature:auth:api`

## Responsibility

Предоставление API для фичи авторизации: `AuthFeatureApi` (маршруты экранов входа и
профиля, регистрация навигационного графа с колбэком `onFinish`).

## Module dependency graph

<!--region graph-->

```mermaid
graph TB
    :feature:auth:api --> :core:feature-api

    classDef android-library fill:#9BF6FF,stroke:#000,stroke-width:2px,color:#000;
```
