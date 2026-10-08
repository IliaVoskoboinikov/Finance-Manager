# Правила ревью PR

Finance Manager — offline-first Android-приложение (Kotlin, Jetpack Compose, Hilt, Room,
Navigation 3, WorkManager). Модули: `app`, `core:*`, `feature:*:api` / `feature:*:impl`, `sync`.
Файл читают AI-ревьюеры (CodeRabbit, Copilot). Правила по областям кода — в
`.coderabbit.yaml` (`path_instructions`), подробные гайды — в `docs/agents/*.md`.

## Как ревьюить

- Пиши **на русском**, коротко: что не так, чем это грозит, как исправить — с конкретным
  вариантом кода, а не «стоит подумать».
- Помечай важность:
  - 🔴 **блокер** — баг, потеря или порча данных, утечка секретов/PII, падение, гонка;
  - 🟠 **важно** — нарушение архитектуры, новая логика без тестов, несоответствие доке;
  - 🟡 **предложение** — читаемость, упрощение, необязательные улучшения.
- Не комментируй то, что уже проверяет CI:
  - форматирование и стиль (ktlint);
  - сложность, длину, магические числа, захардкоженные `Dispatchers.*` (detekt);
  - механику Compose-API: параметр `modifier` и его применение к корню, порядок параметров,
    `MutableState` и изменяемые коллекции в параметрах, лямбды в эффектах без
    `rememberUpdatedState`, имена событий (`onClick`, а не `onClicked`), приватность
    `@Preview`, Material 2 (detekt + `io.nlopez.compose.rules`);
  - `java.util.Date` / `Calendar` (lint `OldDate`, Konsist);
  - пакеты UseCase/Repository, `Entity` вне `core:database`, DTO вне `..dto..`,
    `@HiltViewModel` (Konsist);
  - рёбра между модулями (`assertModuleGraph`, `CheckConventionsPlugin`);
  - совпадение аннотаций навигации с бейзлайнами `*/nav/*.nav` (`navCheck`);
  - порог покрытия (Kover).
- Не предлагай переименований «по вкусу» и смены публичного API без явной причины.

## Осознанные решения — не предлагать «исправить»

- Нет Room `@ForeignKey` / `@Index`: связи логические и разрешаются в коде (offline-first,
  раздельные `localId` / `serverId`, мягкое удаление через `PENDING_DELETE`).
- `fallbackToDestructiveMigration`: до релиза реальных пользователей нет, данные
  пересинхронизируются с сервера.
- Логирование через `android.util.Log` — принятая в проекте конвенция.
- `usesCleartextTraffic` и HTTP допустимы, пока `BuildConfig.HOST` указывает на тестовый стенд
  (`http://yourflow.pro/`); переход на HTTPS перед прод-бэкендом — в `TODO.md`.
- Compose UI не покрыт тестами и исключён из Kover — Compose-тесты отложены осознанно.
- `lifecycle-viewmodel-navigation3` зафиксирован на 2.10.x: 2.11 требует `compileSdk 37`.

## Инварианты проекта

- **Слои.** `core:domain` — чистый Kotlin; Room `Entity`, DTO, Retrofit `Response`,
  `HttpException` не выходят за data-слой; бизнес-логики в Composable и ViewModel нет —
  она в UseCase.
- **Ошибки.** Репозитории и UseCase возвращают `DomainResult` (или `Flow<DomainResult>`),
  исключения через границы слоёв не летят; ViewModel мапит результат в `UiState`.
- **Данные.** Room — единственный источник истины, сеть — только синхронизация; порядок
  синка категории → счета → транзакции, конфликты — last-write-wins. Деньги — `BigDecimal`.
- **Корутины.** Диспетчеры инжектятся; без `GlobalScope` и `runBlocking`;
  `CancellationException` не глотается; UI собирает Flow через `collectAsStateWithLifecycle()`.
- **Навигация.** Экран адресуется `@Serializable NavKey` из `:api`; фича регистрирует только
  свои ключи и ходит к другим через `navigator.goTo(...)`; аргументы во ViewModel — через
  assisted factory, не через `SavedStateHandle`.
- **Безопасность.** Шифрование — только `CryptoManager` (`core:security`); секретов в коде нет;
  токены, PIN и персональные данные не попадают в логи и в `ErrorLogger`; сырой PIN не
  поднимается в presentation-слой.

## Что должно сопровождать изменение

- Новая логика UseCase / ViewModel / репозитория — юнит-тесты: успех, граничные случаи, ошибки.
- Новые публичные классы и функции — KDoc; новый UI-компонент — `@Preview`.
- Изменение схемы Room — поднятая `version` в `@Database`.
- Изменение навигации — аннотации `@NavDestination` / `@NavEdge` / `@NavPreview` и обновлённые
  `*/nav/*.nav` (`./gradlew navDump`).
- Новая строка — в `values/` и `values-ru/`.
- Изменённый модуль — обновлённый `README.md`; новый модуль — ещё и `settings.gradle.kts`
  и `docs/modules.md`; сложная сквозная фича — дизайн-документ в `docs/`.
