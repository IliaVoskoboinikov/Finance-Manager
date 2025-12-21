package soft.divan.financemanager.core.data.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import soft.divan.financemanager.core.data.error.DataError
import soft.divan.financemanager.core.data.mapper.toDomainError
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.logging_error.logging_error_api.ErrorLogger
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
                emit(DomainResult.Failure(DataError.LocalDb(error).toDomainError()))
            }
            .collect { result ->
                emit(result)
            }
    }.flowOn(ctx)

suspend fun <T : Any?> safeDbCall(
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
                DomainResult.Failure(DataError.LocalDb(error).toDomainError())
            }
        )
}
