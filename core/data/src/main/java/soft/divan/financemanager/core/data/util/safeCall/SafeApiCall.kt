package soft.divan.financemanager.core.data.util.safeCall

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import soft.divan.financemanager.core.data.error.DataError
import soft.divan.financemanager.core.data.mapper.toDomainError
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.loggingerror.ErrorLogger
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.CoroutineContext

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
                            DomainResult.Success(Unit as T)
                        }
                    }

                    response.code() == 404 -> {
                        errorLogger.recordError(response.message())
                        DomainResult.Failure(DataError.NotFound.toDomainError())
                    }

                    response.code() == 401 -> {
                        errorLogger.recordError(response.message())
                        DomainResult.Failure(DataError.Unauthorized.toDomainError())
                    }

                    response.code() in 500..599 -> {
                        errorLogger.recordError(response.message())
                        DomainResult.Failure(DataError.Server.toDomainError())
                    }

                    else -> {
                        errorLogger.recordError(response.message())
                        DomainResult.Failure(
                            DataError.Unknown(
                                Throwable(response.message())
                            ).toDomainError()
                        )
                    }
                }
            },
            onFailure = { error ->
                errorLogger.recordError(error.message)
                when (error) {
                    is UnknownHostException,
                    is ConnectException,
                    is SocketTimeoutException -> DomainResult.Failure(
                        DataError.Network.toDomainError()
                    )

                    else ->
                        DomainResult.Failure(DataError.Unknown(error).toDomainError())
                }
            }
        )
}
