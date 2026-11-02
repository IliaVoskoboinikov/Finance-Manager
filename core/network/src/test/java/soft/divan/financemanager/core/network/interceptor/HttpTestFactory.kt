package soft.divan.financemanager.core.network.interceptor

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * Helpers for building real OkHttp [Request] / [Response] objects in interceptor unit tests,
 * so we avoid spinning up a MockWebServer.
 */
internal object HttpTestFactory {

    fun request(
        url: String = "https://example.com/v1/test",
        method: String = "GET",
        body: String? = null
    ): Request =
        Request.Builder()
            .url(url)
            .method(method, body?.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

    fun response(
        request: Request,
        code: Int,
        body: String = "{}"
    ): Response =
        Response.Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("status $code")
            .body(body.toResponseBody("application/json".toMediaTypeOrNull()))
            .build()
}
