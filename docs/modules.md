# Модули проекта

Ссылки на описания всех модулей проекта **Finance Manager**, сгруппированные по типам.
Как модули зависят друг от друга и почему так разбиты — в [modularization.md](./modularization.md).

Каждый модуль содержит собственный `README.md`, где описаны его:

- ответственность;
- зависимости;

---

## Приложение

- [app](../app/README.md) — сборка всех модулей, корневая навигация, DI уровня приложения

---

## Core-модули

- [core:common](../core/common/README.md)
- [core:database](../core/database/README.md)
- [core:data](../core/data/README.md)
- [core:domain](../core/domain/README.md)
- [core:network](../core/network/README.md)
- [core:auth](../core/auth/README.md)
- [core:security](../core/security/README.md)
- [core:uikit](../core/uikit/README.md)
- [core:feature-api](../core/feature-api/README.md)
- [core:logging-error](../core/logging-error/README.md)
- [core:notifications](../core/notifications/README.md)
- [core:workmanager](../core/workmanager/README.md)

---

## Feature-модули

### Счета и операции

- [feature:my-accounts:api](../feature/my-accounts/api/README.md)
- [feature:my-accounts:impl](../feature/my-accounts/impl/README.md)
- [feature:account:api](../feature/account/api/README.md)
- [feature:account:impl](../feature/account/impl/README.md)
- [feature:category:api](../feature/category/api/README.md)
- [feature:category:impl](../feature/category/impl/README.md)
- [feature:transaction:api](../feature/transaction/api/README.md)
- [feature:transaction:impl](../feature/transaction/impl/README.md)
- [feature:history:api](../feature/history/api/README.md)
- [feature:history:impl](../feature/history/impl/README.md)
- [feature:transactions-today:api](../feature/transactions-today/api/README.md)
- [feature:transactions-today:impl](../feature/transactions-today/impl/README.md)
- [feature:analysis:api](../feature/analysis/api/README.md)
- [feature:analysis:impl](../feature/analysis/impl/README.md)

### Настройки и оформление

- [feature:settings:api](../feature/settings/api/README.md)
- [feature:settings:impl](../feature/settings/impl/README.md)
- [feature:design-app:api](../feature/design-app/api/README.md)
- [feature:design-app:impl](../feature/design-app/impl/README.md)
- [feature:languages:api](../feature/languages/api/README.md)
- [feature:languages:impl](../feature/languages/impl/README.md)
- [feature:sounds:api](../feature/sounds/api/README.md)
- [feature:sounds:impl](../feature/sounds/impl/README.md)
- [feature:haptics:api](../feature/haptics/api/README.md)
- [feature:haptics:impl](../feature/haptics/impl/README.md)

### Авторизация, безопасность и система

- [feature:auth:api](../feature/auth/api/README.md)
- [feature:auth:impl](../feature/auth/impl/README.md)
- [feature:security:api](../feature/security/api/README.md)
- [feature:security:impl](../feature/security/impl/README.md)
- [feature:synchronization:api](../feature/synchronization/api/README.md)
- [feature:synchronization:impl](../feature/synchronization/impl/README.md)
- [feature:splash-screen:api](../feature/splash-screen/api/README.md)
- [feature:splash-screen:impl](../feature/splash-screen/impl/README.md)

---

## Вспомогательные модули

- [sync](../sync/README.md) — фоновая синхронизация через WorkManager
- [lint](../lint/README.md) — кастомные правила Android Lint
- [konsist](../konsist/README.md) — архитектурные тесты уровня классов (без продуктового кода)
