package soft.divan.financemanager.core.data.source.impl

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import retrofit2.Response
import soft.divan.financemanager.core.data.api.CategoryApiService
import soft.divan.financemanager.core.data.dto.CategoryDto

class CategoryRemoteDataSourceImplTest {

    private val apiService = mockk<CategoryApiService>()
    private val dataSource = CategoryRemoteDataSourceImpl(apiService)

    private val dto = CategoryDto(
        id = "1",
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z",
        name = "Food",
        emoji = "🍔",
        isIncome = false
    )

    @Test
    fun `getAll delegates to api service`() = runTest {
        val response = Response.success(listOf(dto))
        coEvery { apiService.getAll() } returns response

        assertThat(dataSource.getAll()).isSameAs(response)
    }

    @Test
    fun `getByType delegates to api service`() = runTest {
        val response = Response.success(listOf(dto))
        coEvery { apiService.getByType(true) } returns response

        assertThat(dataSource.getByType(true)).isSameAs(response)
    }
}
