package soft.divan.financemanager.core.data.source.impl

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import retrofit2.Response
import soft.divan.financemanager.core.data.api.TransactionApiService
import soft.divan.financemanager.core.data.dto.TransactionDto
import soft.divan.financemanager.core.data.dto.TransactionRequestDto
import soft.divan.financemanager.core.data.dto.UpdateTransactionRequestDto
import java.math.BigDecimal

class TransactionRemoteDataSourceImplTest {

    private val apiService = mockk<TransactionApiService>()
    private val dataSource = TransactionRemoteDataSourceImpl(apiService)

    private val dto = TransactionDto(
        id = "server-t1",
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z",
        accountId = "server-a1",
        categoryId = "cat-1",
        amount = BigDecimal("42.42"),
        dateTime = "2024-01-15T10:00:00Z",
        comment = "lunch"
    )

    @Test
    fun `create delegates to api service`() = runTest {
        val request = TransactionRequestDto(
            accountId = "server-a1",
            categoryId = "cat-1",
            amount = BigDecimal("42.42"),
            dateTime = "2024-01-15T10:00:00Z"
        )
        val response = Response.success(dto)
        coEvery { apiService.createTransaction(request) } returns response

        assertThat(dataSource.create(request)).isSameAs(response)
    }

    @Test
    fun `getByAccountAndPeriod delegates to api service`() = runTest {
        val response = Response.success(listOf(dto))
        coEvery {
            apiService.getTransactionsByAccountAndPeriod("server-a1", "2024-01-01", "2024-01-31")
        } returns response

        assertThat(
            dataSource.getByAccountAndPeriod("server-a1", "2024-01-01", "2024-01-31")
        ).isSameAs(response)
    }

    @Test
    fun `getByAccountAndPeriod passes null period bounds`() = runTest {
        val response = Response.success(listOf(dto))
        coEvery {
            apiService.getTransactionsByAccountAndPeriod("server-a1", null, null)
        } returns response

        assertThat(dataSource.getByAccountAndPeriod("server-a1")).isSameAs(response)
    }

    @Test
    fun `get delegates to api service`() = runTest {
        val response = Response.success(dto)
        coEvery { apiService.getTransaction("server-t1") } returns response

        assertThat(dataSource.get("server-t1")).isSameAs(response)
    }

    @Test
    fun `update delegates to api service`() = runTest {
        val request = UpdateTransactionRequestDto(
            accountId = "server-a1",
            categoryId = "cat-1",
            amount = BigDecimal("42.42"),
            dateTime = "2024-01-15T10:00:00Z"
        )
        val response = Response.success(Unit)
        coEvery { apiService.updateTransaction("server-t1", request) } returns response

        assertThat(dataSource.update("server-t1", request)).isSameAs(response)
    }

    @Test
    fun `delete delegates to api service`() = runTest {
        val response = Response.success(Unit)
        coEvery { apiService.deleteTransaction("server-t1") } returns response

        assertThat(dataSource.delete("server-t1")).isSameAs(response)
    }
}
