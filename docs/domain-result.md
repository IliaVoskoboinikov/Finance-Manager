# DomainResult & Error handling

В этом документе описан паттерн **`DomainResult`** и подход к обработке ошибок
в доменном слое (**core:domain**) проекта **Finance Manager**.

## Задача, которую решает DomainResult

В доменном слое нам нужно:

- единообразно представлять результат операций (успех / ошибка);
- не протаскивать исключения (`Throwable`) напрямую в UI;
- уметь детально различать типы ошибок (нет данных, нет сети, нет прав, неизвестная).

Обычный `Result<T>` из стандартной библиотеки не подходит полностью, потому что:

- он завязан на `Throwable` как причину;
- нам нужна своя, доменная иерархия ошибок (`DomainError`).

Поэтому введён свой тип:

```kotlin
sealed interface DomainResult<out T> {
    data class Success<T>(val data: T) : DomainResult<T>
    data class Failure(val error: DomainError) : DomainResult<Nothing>
}
```

Где `DomainError` — отдельная иерархия:

```kotlin
sealed interface DomainError : AppError {

    data object NoData : DomainError
    data object Unauthorized : DomainError
    data object OperationNotAllowed : DomainError
    data object NetworkUnavailable : DomainError

    data class Unknown(
        override val cause: Throwable?
    ) : DomainError
}
```

Таким образом:

- **успех** — `DomainResult.Success<T>(data)`;
- **ошибка** — `DomainResult.Failure(DomainError)`.

## Вспомогательные функции

Для удобной работы с `DomainResult` используются расширения:

```kotlin
inline fun <T, R> DomainResult<T>.fold(
    onSuccess: (T) -> R,
    onFailure: (DomainError) -> R
): R = when (this) {
    is DomainResult.Success -> onSuccess(data)
    is DomainResult.Failure -> onFailure(error)
}

inline fun <T> DomainResult<T>.onSuccess(
    action: (T) -> Unit
): DomainResult<T>

inline fun <T> DomainResult<T>.onFailure(
    action: (DomainError) -> Unit
): DomainResult<T>

fun <T> DomainResult<T>.getOrNull(): T?
```

Паттерны использования:

- в use case’ах — через `fold` и композицию нескольких `DomainResult`;
- в ViewModel — через `onSuccess` / `onFailure` для обновления `UiState`.

## В доменных интерфейсах

Интерфейсы репозиториев и use case’ов в `core:domain` возвращают `DomainResult`:

```kotlin
interface GetAccountsUseCase {
    operator fun invoke(): Flow<DomainResult<List<Account>>>
}

interface AccountRepository {
    suspend fun create(account: Account): DomainResult<Unit>
    fun getAll(): Flow<DomainResult<List<Account>>>
    suspend fun update(account: Account): DomainResult<Unit>
    suspend fun delete(id: String): DomainResult<Unit>
    suspend fun getById(id: String): DomainResult<Account>
}
```

Для реактивных сценариев (`Flow`) мы используем `Flow<DomainResult<T>>`, что позволяет:

- обновлять данные (Success) без прерывания стрима;
- сигнализировать об ошибке (Failure) в том же потоке, не бросая исключения наружу.

## Пример композиции результатов

Интересный пример — `GetTransactionsByPeriodUseCaseImpl`, где нужно:

- получить несколько наборов данных (аккаунты, категории, транзакции);
- объединить их и учесть разные сценарии ошибок.

Упрощённая структура:

```kotlin
// 1. Базовые данные: аккаунты + валюта + категории
private fun baseDataFlow():
        Flow<DomainResult<Triple<List<Account>, CurrencySymbol, List<Category>>>> = ...

// 2. На основе baseDataFlow собираем транзакции за период
override fun invoke(...): Flow<DomainResult<Triple<List<Transaction>, CurrencySymbol, List<Category>>>> =
    baseDataFlow().flatMapLatest { baseResult ->
        when (baseResult) {
            is DomainResult.Failure -> flowOf(DomainResult.Failure(baseResult.error))
            is DomainResult.Success -> fetchAndCombineTransactions(...)
        }
    }
```

При работе с несколькими `DomainResult` используется подход:

- если хотя бы один из результатов — `Failure`, возвращаем первую ошибку;
- если все `Success`, собираем итоговые данные.

Псевдокод:

```kotlin
val results: Array<DomainResult<List<Transaction>>> = ...
val failure = results.firstOrNull { it is DomainResult.Failure }
if (failure != null) {
    return DomainResult.Failure(failure.error)
}

val allTransactions = results
    .filterIsInstance<DomainResult.Success<List<Transaction>>>()
    .flatMap { it.data }

return DomainResult.Success(Triple(allTransactions, currency, categories))
```

## Использование в UI

В ViewModel типичный паттерн:

```kotlin
viewModelScope.launch {
    getAccountsUseCase()
        .collect { result ->
            result.fold(
                onSuccess = { accounts ->
                    _uiState.value = UiState.Success(accounts)
                },
                onFailure = { error ->
                    _uiState.value = UiState.Error(mapErrorToUiMessage(error))
                }
            )
        }
}
```

Где:

- `UiState.Success` хранит готовые для отображения данные;
- `UiState.Error` хранит либо enum / код ошибки, либо `@StringRes` для текста сообщения.

Так UI:

- знает про "свои" типы состояний (Loading / Success / Error / Empty);
- не знает деталей сетевых/бизнес‑ошибок — только их проекцию на сообщения и сценарии поведения.

## Почему так, а не Exceptions?

- Исключения удобны как **механизм сигнализации**, но:
    - плохо композиционируются, когда нужно собрать несколько источников данных;
    - их легко "забыть" обработать, особенно внутри сложных flow‑пайплайнов.
- `DomainResult`:
    - явно заставляет обработать оба случая (`Success` и `Failure`);
    - легко комбинируется и маппится;
    - хорошо ложится на корутины и `Flow`.

В итоге:

- **Data layer** может использовать исключения и `Result` для внутренних задач.
- **Domain layer** всегда возвращает **`DomainResult`**, приводя внешние ошибки к доменной модели.
- **UI layer** работает с `UiState`, в котором ошибки уже отображаются в понятном для пользователя виде.

