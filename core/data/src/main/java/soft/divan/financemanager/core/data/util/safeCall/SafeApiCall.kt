package soft.divan.financemanager.core.data.util.safeCall

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import soft.divan.financemanager.core.data.error.DataError
import soft.divan.financemanager.core.data.mapper.toDomainError
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.loggingerror.ErrorLogger
import soft.divan.financemanager.core.auth.data.interceptor.GuestModeNetworkBlockedException
import soft.divan.financemanager.core.auth.data.interceptor.UnauthorizedNetworkBlockedException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.CoroutineContext

private const val HTTP_NOT_FOUND = 404
private const val HTTP_UNAUTHORIZED = 401
private const val HTTP_SERVER_ERROR_MIN = 500
private const val HTTP_SERVER_ERROR_MAX = 599

/**
 * Безопасный вызов API.
 *
 * Поведение при успешном ответе без тела зависит от типа [T]:
 * - если [T] это [Unit] (no-content эндпоинты: update/delete/logout) — возвращается `Success(Unit)`;
 * - иначе пустое тело трактуется как отсутствие данных (`Failure(NotFound)`), а не приводится
 *   к [Unit] небезопасным кастом (иначе у типизированного эндпоинта был бы «сломанный» Success
 *   и ClassCastException у вызывающего).
 */
suspend inline fun <reified T : Any> safeApiCall(
    errorLogger: ErrorLogger,
    ctx: CoroutineContext = Dispatchers.IO,
    noinline call: suspend () -> Response<T>
): DomainResult<T> {
    @Suppress("UNCHECKED_CAST")
    return safeApiCallInternal(
        errorLogger = errorLogger,
        ctx = ctx,
        allowEmptyBody = T::class == Unit::class,
        call = call
    ) as DomainResult<T>
}

@PublishedApi
internal suspend fun safeApiCallInternal(
    errorLogger: ErrorLogger,
    ctx: CoroutineContext,
    allowEmptyBody: Boolean,
    call: suspend () -> Response<*>
): DomainResult<Any> = withContext(ctx) {
    runCatching { call() }.fold(
        onSuccess = { response ->
            if (response.isSuccessful) {
                val body = response.body()
                when {
                    body != null -> DomainResult.Success(body)

                    allowEmptyBody -> DomainResult.Success(Unit)

                    else -> {
                        errorLogger.recordError("Empty body for successful response ${response.code()}")
                        DomainResult.Failure(DataError.NotFound.toDomainError())
                    }
                }
            } else {
                response.toFailure(errorLogger)
            }
        },
        onFailure = { it.toFailure(errorLogger) }
    )
}

/** Маппит неуспешный HTTP-ответ в доменную ошибку. */
private fun Response<*>.toFailure(errorLogger: ErrorLogger): DomainResult.Failure {
    errorLogger.recordError("HTTP ${code()} ${message().orEmpty()}")
    val dataError = when {
        code() == HTTP_NOT_FOUND -> DataError.NotFound
        code() == HTTP_UNAUTHORIZED -> DataError.Unauthorized
        code() in HTTP_SERVER_ERROR_MIN..HTTP_SERVER_ERROR_MAX -> DataError.Server
        else -> DataError.Unknown(Throwable(message()))
    }
    return DomainResult.Failure(dataError.toDomainError())
}

/** Маппит исключение сетевого вызова в доменную ошибку. */
private fun Throwable.toFailure(errorLogger: ErrorLogger): DomainResult.Failure =
    when (this) {
        is GuestModeNetworkBlockedException ->
            DomainResult.Failure(DataError.GuestMode.toDomainError())

        is UnauthorizedNetworkBlockedException ->
            DomainResult.Failure(DataError.UnauthorizedBlocked.toDomainError())

        is UnknownHostException,
        is ConnectException,
        is SocketTimeoutException ->
            DomainResult.Failure(DataError.Network.toDomainError())

        else -> {
            errorLogger.recordError(this)
            DomainResult.Failure(DataError.Unknown(this).toDomainError())
        }
    }
