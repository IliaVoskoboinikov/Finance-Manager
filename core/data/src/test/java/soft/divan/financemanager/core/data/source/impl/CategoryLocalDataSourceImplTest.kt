package soft.divan.financemanager.core.data.source.impl

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.database.dao.CategoryDao
import soft.divan.financemanager.core.database.entity.CategoryEntity

class CategoryLocalDataSourceImplTest {

    private val dao = mockk<CategoryDao>(relaxUnitFun = true)
    private val dataSource = CategoryLocalDataSourceImpl(dao)

    private val entity = CategoryEntity(
        id = "1",
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z",
        name = "Food",
        emoji = "🍔",
        isIncome = false
    )

    @Test
    fun `insert delegates to dao insertAll`() = runTest {
        dataSource.insert(listOf(entity))

        coVerify(exactly = 1) { dao.insertAll(listOf(entity)) }
    }

    @Test
    fun `getAll delegates to dao`() = runTest {
        val flow = flowOf(listOf(entity))
        coEvery { dao.getAll() } returns flow

        assertThat(dataSource.getAll()).isSameAs(flow)
    }

    @Test
    fun `getByType delegates to dao`() = runTest {
        val flow = flowOf(listOf(entity))
        coEvery { dao.getByType(false) } returns flow

        assertThat(dataSource.getByType(false)).isSameAs(flow)
    }

    @Test
    fun `getById delegates to dao`() = runTest {
        coEvery { dao.getById("1") } returns entity

        assertThat(dataSource.getById("1")).isEqualTo(entity)
    }

    @Test
    fun `getById returns null for missing category`() = runTest {
        coEvery { dao.getById("missing") } returns null

        assertThat(dataSource.getById("missing")).isNull()
    }
}
