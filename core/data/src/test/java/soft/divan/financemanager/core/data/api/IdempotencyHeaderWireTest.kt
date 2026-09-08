package soft.divan.financemanager.core.data.api

import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import soft.divan.financemanager.core.data.dto.CreateAccountRequestDto
import soft.divan.financemanager.core.data.dto.TransactionRequestDto
import soft.divan.financemanager.core.data.dto.UpdateAccountRequestDto
import soft.divan.financemanager.core.data.dto.UpdateTransactionRequestDto
import java.math.BigDecimal

/**
 * Проверка уровня провода: Retrofit действительно кладёт ключ идемпотентности в HTTP-заголовок.
 *
 * Проверки на моках подтверждают только то, что значение доехало до слоя API; здесь собирается
 * настоящий [Request] по аннотациям, и он осматривается до отправки. Сеть при этом не нужна —
 * интерсептор замыкает цепочку и возвращает синтетический ответ.
 */
class IdempotencyHeaderWireTest {

    /** Ловит собранный Retrofit'ом запрос и отдаёт вместо сети пустой JSON-ответ. */
    private class CapturingInterceptor : Interceptor {
        lateinit var request: Request
            private set

        override fun intercept(chain: Interceptor.Chain): Response {
            request = chain.request()
            return Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body("{}".toResponseBody("application/json".toMediaType()))
                .build()
        }
    }

    private val interceptor = CapturingInterceptor()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://example.invalid/")
        .client(OkHttpClient.Builder().addInterceptor(interceptor).build())
        .addConverterFactory(GsonConverterFactory.create(Gson()))
        .build()

    private val transactionApi = retrofit.create(TransactionApiService::class.java)
    private val accountApi = retrofit.create(AccountApiService::class.java)

    private fun transactionRequest() = TransactionRequestDto(
        id = "local-t1",
        accountId = "local-a1",
        categoryId = "cat-1",
        amount = BigDecimal("42.42"),
        dateTime = "2024-01-15T10:00:00Z",
        comment = "lunch"
    )

    private fun accountRequest() = CreateAccountRequestDto(
        name = "Cash",
        balance = BigDecimal.TEN,
        currencyId = "rub-id"
    )

    private val sentKey get() = interceptor.request.header(IDEMPOTENCY_KEY_HEADER)

    /* ---------- транзакции ---------- */

    @Test
    fun `POST transaction carries the idempotency key header`() = runTest {
        transactionApi.createTransaction(transactionRequest(), "op-key-1")

        assertThat(sentKey).isEqualTo("op-key-1")
        assertThat(interceptor.request.method).isEqualTo("POST")
    }

    @Test
    fun `PUT transaction carries the idempotency key header`() = runTest {
        transactionApi.updateTransaction(
            id = "server-t1",
            request = UpdateTransactionRequestDto(
                accountId = "local-a1",
                categoryId = "cat-1",
                amount = BigDecimal("42.42"),
                dateTime = "2024-01-15T10:00:00Z",
                comment = null
            ),
            idempotencyKey = "op-key-2"
        )

        assertThat(sentKey).isEqualTo("op-key-2")
        assertThat(interceptor.request.method).isEqualTo("PUT")
    }

    @Test
    fun `DELETE transaction carries the idempotency key header`() = runTest {
        transactionApi.deleteTransaction(id = "server-t1", idempotencyKey = "op-key-3")

        assertThat(sentKey).isEqualTo("op-key-3")
        assertThat(interceptor.request.method).isEqualTo("DELETE")
    }

    /* ---------- счета ---------- */

    @Test
    fun `POST account carries the idempotency key header`() = runTest {
        accountApi.createAccount(accountRequest(), "op-key-4")

        assertThat(sentKey).isEqualTo("op-key-4")
    }

    @Test
    fun `PUT account carries the idempotency key header`() = runTest {
        accountApi.updateAccount(
            id = "server-a1",
            request = UpdateAccountRequestDto(
                name = "Cash",
                balance = BigDecimal.TEN,
                currencyId = "rub-id"
            ),
            idempotencyKey = "op-key-5"
        )

        assertThat(sentKey).isEqualTo("op-key-5")
    }

    @Test
    fun `DELETE account carries the idempotency key header`() = runTest {
        accountApi.delete(id = "server-a1", idempotencyKey = "op-key-6")

        assertThat(sentKey).isEqualTo("op-key-6")
    }

    /* ---------- чтение ---------- */

    @Test
    fun `GET requests carry no idempotency key`() = runTest {
        transactionApi.getTransaction("server-t1")

        // Чтение безопасно по определению — дедуплицировать нечего
        assertThat(sentKey).isNull()
    }

    @Test
    fun `header name matches the de facto standard`() = runTest {
        transactionApi.createTransaction(transactionRequest(), "op-key-1")

        // Имя заголовка — часть контракта с сервером, опечатка здесь тихо отключит дедупликацию
        assertThat(IDEMPOTENCY_KEY_HEADER).isEqualTo("Idempotency-Key")
        assertThat(interceptor.request.headers.names()).contains("Idempotency-Key")
    }
}
