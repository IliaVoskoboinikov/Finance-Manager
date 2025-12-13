package soft.divan.financemanager.core.data.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.Response
import soft.divan.financemanager.core.domain.util.DomainResult
import soft.divan.financemanager.core.domain.util.ErrorType
import soft.divan.financemanager.core.logging_error.logging_error_api.ErrorLogger
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.CoroutineContext

fun <T : Any> safeDbFlow(
    errorLogger: ErrorLogger,
    ctx: CoroutineContext = Dispatchers.IO,
    block: suspend () -> Flow<T>
): Flow<DomainResult<T>> =
    flow {
        block()
            .map { DomainResult.Success(it) }
            .catch { error ->
                errorLogger.recordError(error.message)
                emit(DomainResult.Failure(ErrorType.LocalDb, error.message))
            }
            .collect { result ->
                emit(result)
            }
    }.flowOn(ctx)

suspend fun <T : Any> safeDbCall(
    errorLogger: ErrorLogger,
    ctx: CoroutineContext = Dispatchers.IO,
    call: suspend () -> T
): DomainResult<T> = withContext(ctx) {
    runCatching { call() }
        .fold(
            onSuccess = { result ->
                DomainResult.Success(result)
            },
            onFailure = { error ->
                errorLogger.recordError(error.message)
                DomainResult.Failure(ErrorType.LocalDb, error.message)
            }
        )
}

suspend fun <T : Any> safeApiCall(
    errorLogger: ErrorLogger,
    ctx: CoroutineContext = Dispatchers.IO,
    call: suspend () -> Response<T>
): DomainResult<T> = withContext(ctx) {
    runCatching { call() }
        .fold(
            onSuccess = { response ->
                when {
                    response.isSuccessful -> {
                        val body = response.body()
                        if (body != null) {
                            DomainResult.Success(body)
                        } else {
                            errorLogger.recordError(response.message())
                            DomainResult.Failure(ErrorType.NoData, response.message())
                        }
                    }

                    response.code() == 401 -> {
                        errorLogger.recordError(response.message())
                        DomainResult.Failure(ErrorType.Unauthorized, response.message())
                    }

                    response.code() in 500..599 -> {
                        errorLogger.recordError(response.message())
                        DomainResult.Failure(ErrorType.Server, response.message())
                    }

                    else -> {
                        errorLogger.recordError(response.message())
                        DomainResult.Failure(ErrorType.Unknown, response.message())
                    }
                }
            },
            onFailure = { error ->
                errorLogger.recordError(error.message)
                when (error) {
                    is UnknownHostException,
                    is ConnectException,
                    is SocketTimeoutException -> DomainResult.Failure(ErrorType.Network)

                    else ->
                        DomainResult.Failure(ErrorType.Unknown, error.message)
                }
            }
        )
}
