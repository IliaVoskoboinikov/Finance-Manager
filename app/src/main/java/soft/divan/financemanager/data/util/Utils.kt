package soft.divan.financemanager.data.util

import android.util.Log
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import retrofit2.Response
import soft.divan.financemanager.data.network.interceptor.NoInternetException
import soft.divan.financemanager.domain.utils.Exzeption
import soft.divan.financemanager.domain.utils.Reazon
import soft.divan.financemanager.domain.utils.Rezult

import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.CoroutineContext

suspend fun <T : Any?> safeResult(
    ctx: CoroutineContext = IO,
    call: suspend () -> T
): Rezult<T> = try {
    val data = withContext(ctx) { call.invoke() }
    Rezult.Success(data)
} catch (e: SocketTimeoutException) {
    Log.e("safeResult", "SocketTimeoutException: ${e.message}", e)
    Rezult.Error(Exzeption(Reazon.NETWORK_ERROR, e))
} catch (e: ConnectException) {
    Log.e("safeResult", "ConnectException: ${e.message}", e)
    Rezult.Error(Exzeption(Reazon.NETWORK_ERROR, e))
} catch (e: Exception) {
    Log.e("safeResult", "Exception: ${e.message}", e)
    Rezult.Error(Exzeption(Reazon.UNKNOWN_ERROR, e))
} catch (e: Throwable) {
    Log.e("safeResult", "Throwable: ${e.message}", e)
    Rezult.Error(Exzeption(Reazon.INTERNAL_ERROR, e))
}

suspend fun <T : Any?> safeHttpResult(
    ctx: CoroutineContext = IO,
    call: suspend () -> Response<T>
): Rezult<T> = try {
    val rsp = withContext(ctx) { call.invoke() }
    if (rsp.isSuccessful) {
        rsp.body()
            ?.let { Rezult.Success(it) }
            ?: Rezult.Error(Reazon.NULL_DATA)
    } else {
        val body = runCatching { rsp.errorBody().toString() }
        if (body.isSuccess) {
            if (rsp.code() == 401) {
                Log.e("safeHttpResult", "401 Unauthorized: ${body.getOrNull()}")
                Rezult.Error(Exzeption(Reazon.BAD_AUTH, data = body.getOrNull()))
            } else {
                Log.e("safeHttpResult", "HTTP error ${rsp.code()}: ${body.getOrNull()}")
                Rezult.Error(Exzeption(Reazon.WITH_ERROR_MESSAGE, data = body.getOrNull()))
            }
        } else {
            Log.e("safeHttpResult", "HTTP error ${rsp.code()}, failed to read error body")
            Rezult.Error(Exzeption(Reazon.SERVER_ERROR))
        }
    }
} catch (e: NoInternetException) {
    Log.e("safeHttpResult", "NoInternetException: ${e.message}", e)
    Rezult.Error(Exzeption(Reazon.NO_NETWORK_CONNECTION, e))
} catch (e: UnknownHostException) {
    Log.e("safeHttpResult", "UnknownHostException: ${e.message}", e)
    Rezult.Error(Exzeption(Reazon.NETWORK_ERROR, e))
} catch (e: SocketTimeoutException) {
    Log.e("safeHttpResult", "SocketTimeoutException: ${e.message}", e)
    Rezult.Error(Exzeption(Reazon.NETWORK_ERROR, e))
} catch (e: ConnectException) {
    Log.e("safeHttpResult", "ConnectException: ${e.message}", e)
    Rezult.Error(Exzeption(Reazon.NETWORK_ERROR, e))
} catch (e: Exception) {
    Log.e("safeHttpResult", "Exception: ${e.message}", e)
    Rezult.Error(Exzeption(Reazon.UNKNOWN_ERROR, e))
} catch (e: Throwable) {
    Log.e("safeHttpResult", "Throwable: ${e.message}", e)
    Rezult.Error(Exzeption(Reazon.INTERNAL_ERROR, e))
}
