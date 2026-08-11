# Navigation

Проект использует **Navigation 3** (`androidx.navigation3`). Подробности и схемы —
[`docs/navigation3.md`](../navigation3.md).

## Ключи вместо маршрутов
*   Каждый экран адресуется `NavKey`, а не строкой. Ключи объявляются в `:api` модуле фичи,
    обязаны быть `@Serializable` (по ним восстанавливается back stack после смерти процесса).
*   Аргументы экрана — поля ключа (`data class TransactionKey(val isIncome: Boolean, ...)`).
    Никакой ручной конкатенации маршрутов и `navArgument`.

## Границы фичи
*   Фича реализует `FeatureApi.registerEntries(scope, navigator, modifier)` и регистрирует
    в `entryProvider` **только свои** экраны: один ключ можно зарегистрировать лишь однажды.
*   Переход к соседней фиче — `navigator.goTo(ЕёKey)`, возврат — `navigator::back`.
    `NavBackStack`/`NavDisplay` фиче недоступны — ими владеет хост.
*   Новая фича попадает в граф через `@Binds @IntoSet` в `app/di/FeatureNavigationModule.kt`.

## Хост
*   `app` владеет двумя back stack: корневым (`RootNavDisplay`: старт → авторизация →
    главный экран) и стеками вкладок (`TopLevelBackStack`, отдельный стек на вкладку).
*   Стеки создаются через `rememberNavBackStack`, поэтому переживают смерть процесса.
*   `NavDisplay` всегда получает декораторы `rememberSaveableStateHolderNavEntryDecorator()`
    и `rememberViewModelStoreNavEntryDecorator()`.

## ViewModel и аргументы
*   `SavedStateHandle` не содержит аргументов навигации. Если ViewModel нужен аргумент —
    `@HiltViewModel(assistedFactory = ...)` + `@Assisted` и вызов
    `hiltViewModel<VM, VM.Factory> { it.create(...) }` из экрана.
