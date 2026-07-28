package soft.divan.financemanager.core.data.source.impl

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import retrofit2.Response
import soft.divan.financemanager.core.data.api.AccountApiService
import soft.divan.financemanager.core.data.dto.AccountDto
import soft.divan.financemanager.core.data.dto.CreateAccountRequestDto
import soft.divan.financemanager.core.data.dto.UpdateAccountRequestDto
import java.math.BigDecimal

class AccountRemoteDataSourceImplTest {

    private val apiService = mockk<AccountApiService>()
    private val dataSource = AccountRemoteDataSourceImpl(apiService)

    private val dto = AccountDto(
        id = "server-1",
        userId = "u1",
        name = "Cash",
        balance = BigDecimal.TEN,
        currencyId = "rub-id",
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z"
    )

    @Test
    fun `create delegates to api service`() = runTest {
        val request = CreateAccountRequestDto(
            name = "Cash",
            balance = BigDecimal.TEN,
            currencyId = "rub-id"
        )
        val response = Response.success(dto)
        coEvery { apiService.createAccount(request) } returns response

        assertThat(dataSource.create(request)).isSameAs(response)
    }

    @Test
    fun `getAll delegates to api service`() = runTest {
        val response = Response.success(listOf(dto))
        coEvery { apiService.getAccounts() } returns response

        assertThat(dataSource.getAll()).isSameAs(response)
    }

    @Test
    fun `getById delegates to api service`() = runTest {
        val response = Response.success(dto)
        coEvery { apiService.getById("server-1") } returns response

        assertThat(dataSource.getById("server-1")).isSameAs(response)
    }

    @Test
    fun `update delegates to api service`() = runTest {
        val request = UpdateAccountRequestDto(
            name = "Cash",
            balance = BigDecimal.TEN,
            currencyId = "rub-id"
        )
        val response = Response.success(Unit)
        coEvery { apiService.updateAccount("server-1", request) } returns response

        assertThat(dataSource.update("server-1", request)).isSameAs(response)
    }

    @Test
    fun `delete delegates to api service`() = runTest {
        val response = Response.success(Unit)
        coEvery { apiService.delete("server-1") } returns response

        assertThat(dataSource.delete("server-1")).isSameAs(response)
    }
}
