package soft.divan.financemanager.core.data.outbox

import retrofit2.Response
import soft.divan.financemanager.core.auth.data.interceptor.GuestModeNetworkBlockedException
import soft.divan.financemanager.core.auth.data.interceptor.UnauthorizedNetworkBlockedException
import soft.divan.financemanager.core.database.model.OutboxOperation
import java.io.IOException
import java.util.concurrent.CancellationException

private const val HTTP_UNAUTHORIZED = 401
private const val HTTP_NOT_FOUND = 404
private const val HTTP_SERVER_ERROR_MIN = 500
private const val HTTP_SERVER_ERROR_MAX = 599
private val SERVER_ERROR_RANGE = HTTP_SERVER_ERROR_MIN..HTTP_SERVER_ERROR_MAX

/** Что вернул один сетевой вызов отправителя: тело ответа либо готовый исход для очереди. */
internal sealed interface OutboxCallOutcome<out T> {

    data class Ok<T>(val body: T?) : OutboxCallOutcome<T>

    data class Rejected(val result: OutboxSendResult) : OutboxCallOutcome<Nothing>
}

/**
 * Выполняет запрос отправителя и переводит неуспех в исход очереди.
 *
 * Здесь намеренно разбирается HTTP-код, а не `DomainResult`: очереди нужно различать «сервер
 * прилёг» и «сервер отверг данные», а доменные ошибки схлопывают оба случая в один. Отправители
 * живут в data-слое, поэтому работать с [Response] им можно — наружу он не протекает.
 */
internal suspend fun <T : Any> outboxCall(
    operation: OutboxOperation,
    call: suspend () -> Response<T>
): OutboxCallOutcome<T> = runCatching { call() }.fold(
    onSuccess = { response ->
        if (response.isSuccessful) {
            OutboxCallOutcome.Ok(response.body())
        } else {
            OutboxCallOutcome.Rejected(classify(response.code(), operation))
        }
    },
    onFailure = { throwable ->
        // Отмена корутины — не сбой отправки: её нельзя превращать в «повторим позже»,
        // иначе прогон продолжится после остановки.
        if (throwable is CancellationException) throw throwable
        OutboxCallOutcome.Rejected(throwable.classify())
    }
)

/**
 * Переводит исключение сетевого вызова в исход очереди.
 *
 * Сбой сети (равно как и непредвиденный) считается временным: состояние сервера неизвестно,
 * поэтому операцию нужно повторить, а не выбрасывать.
 */
private fun Throwable.classify(): OutboxSendResult = when (this) {
    is GuestModeNetworkBlockedException -> OutboxSendResult.Blocked("гостевой режим: $message")
    is UnauthorizedNetworkBlockedException -> OutboxSendResult.Blocked("нет сессии: $message")
    is IOException -> OutboxSendResult.Transient("сбой сети: $message")
    else -> OutboxSendResult.Transient("непредвиденный сбой: $message")
}

/**
 * Классифицирует неуспешный HTTP-код.
 *
 * - `404` на удалении — идемпотентный успех: ресурса на сервере уже нет, цель достигнута;
 * - `401` — сессия истекла: повторим после обновления токена, попытку не тратим;
 * - `5xx` — сервер временно не может обработать запрос;
 * - остальные `4xx` — запрос отвергнут по существу (валидация), повтор ничего не изменит.
 */
private fun classify(code: Int, operation: OutboxOperation): OutboxSendResult = when {
    code == HTTP_NOT_FOUND && operation == OutboxOperation.DELETE -> OutboxSendResult.Success
    code == HTTP_UNAUTHORIZED -> OutboxSendResult.Blocked("сессия истекла (HTTP $code)")
    code in SERVER_ERROR_RANGE -> OutboxSendResult.Transient("HTTP $code")
    else -> OutboxSendResult.Terminal("HTTP $code")
}
