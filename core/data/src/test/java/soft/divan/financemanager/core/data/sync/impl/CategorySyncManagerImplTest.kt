package soft.divan.financemanager.core.data.sync.impl

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import retrofit2.Response
import soft.divan.financemanager.core.data.dto.CategoryDto
import soft.divan.financemanager.core.data.source.CategoryLocalDataSource
import soft.divan.financemanager.core.data.source.CategoryRemoteDataSource
import soft.divan.financemanager.core.data.sync.util.Synchronizer
import soft.divan.financemanager.core.database.entity.CategoryEntity
import soft.divan.financemanager.core.loggingerror.ErrorLogger

class CategorySyncManagerImplTest {

    private val remoteDataSource = mockk<CategoryRemoteDataSource>()
    private val localDataSource = mockk<CategoryLocalDataSource>(relaxUnitFun = true)
    private val errorLogger = mockk<ErrorLogger>(relaxed = true)

    private val syncManager = CategorySyncManagerImpl(
        remoteDataSource = remoteDataSource,
        localDataSource = localDataSource,
        errorLogger = errorLogger
    )

    private fun dto(id: String) = CategoryDto(
        id = id,
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z",
        name = "Category $id",
        emoji = "🍔",
        isIncome = false
    )

    @Test
    fun `pullServerData stores mapped server categories locally`() = runTest {
        coEvery { remoteDataSource.getAll() } returns Response.success(listOf(dto("1"), dto("2")))
        val inserted = slot<List<CategoryEntity>>()
        coEvery { localDataSource.insert(capture(inserted)) } returns Unit

        syncManager.pullServerData()

        assertThat(inserted.captured.map { it.id }).containsExactly("1", "2")
        assertThat(inserted.captured.first().name).isEqualTo("Category 1")
    }

    @Test
    fun `pullServerData does nothing when server call fails`() = runTest {
        coEvery { remoteDataSource.getAll() } throws RuntimeException("offline")

        syncManager.pullServerData()

        coVerify(exactly = 0) { localDataSource.insert(any()) }
    }

    @Test
    fun `syncWith pulls server data and reports success`() = runTest {
        coEvery { remoteDataSource.getAll() } returns Response.success(emptyList())
        coEvery { localDataSource.insert(any()) } returns Unit

        val result = syncManager.syncWith(object : Synchronizer {})

        assertThat(result).isTrue()
        coVerify(exactly = 1) { remoteDataSource.getAll() }
    }
}
