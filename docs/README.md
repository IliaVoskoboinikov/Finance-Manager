# Finance Manager – Documentation

Добро пожаловать в документацию проекта **Finance Manager**. Здесь собраны отдельные документы,
которые подробно описывают архитектуру, модульную структуру, работу с данными и подход к качеству.

## Содержание

- [Architecture](./architecture.md) — архитектура приложения, слои, потоки данных, ключевые паттерны.
- [Modularization](./modularization.md) — структура модулей, зависимости, принципы разбиения.
- [Modules](./modules.md) — список всех модулей с сылками на их структуру.
- [Navigation 3](./navigation3.md) — навигация на `androidx.navigation3`: ключи, сборка графа, back stack вкладок.
- [Nav graph map](./nav-graph.md) — карта экранов из аннотаций (`compose-nav-graph`): как собирается, как размечать, `navDump` / `navCheck`.
- [Auth & Session](./auth.md) — авторизация, управление JWT и состояниями сессии.
- [Synchronization](./synchronization.md) — как устроена фоновая синхронизация данных.
- [Account status & archiving](./account-archive.md) — статусная модель счёта, удаление и архивация («призрачный» счёт).
- [DomainResult & errors](./domain-result.md) — подход к доменным результатам и обработке ошибок.
- [Bd](./bd.md) — локальная база данных.
- [Testing & Coverage](./testing.md) — уровни тестов, инструменты (MockK, Robolectric, TestKit), измерение покрытия через Kover, как запускать.
- [CI/CD](./ci-cd.md) — workflow GitHub Actions, composite actions, секреты, релизный пайплайн и что в нём ещё не сделано.

