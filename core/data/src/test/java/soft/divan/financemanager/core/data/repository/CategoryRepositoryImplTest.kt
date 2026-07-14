package soft.divan.financemanager.core.data.repository

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import retrofit2.Response
import soft.divan.financemanager.core.data.dto.CategoryDto
import soft.divan.financemanager.core.data.source.CategoryLocalDataSource
import soft.divan.financemanager.core.data.source.CategoryRemoteDataSource
import soft.divan.financemanager.core.data.sync.CategorySyncManager
import soft.divan.financemanager.core.database.entity.CategoryEntity
import soft.divan.financemanager.core.domain.error.DomainError
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.loggingerror.ErrorLogger

class CategoryRepositoryImplTest {

    private val remoteDataSource = mockk<CategoryRemoteDataSource>()
    private val localDataSource = mockk<CategoryLocalDataSource>(relaxUnitFun = true)
    private val syncManager = mockk<CategorySyncManager>(relaxed = true)
    private val appCoroutineContext = RecordingAppCoroutineContext()
    private val errorLogger = mockk<ErrorLogger>(relaxed = true)

    private val repository = CategoryRepositoryImpl(
        remoteDataSource = remoteDataSource,
        localDataSource = localDataSource,
        syncManager = syncManager,
        appCoroutineContext = appCoroutineContext,
        errorLogger = errorLogger
    )

    private fun entity(id: String, isIncome: Boolean = false) = CategoryEntity(
        id = id,
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z",
        name = "Category $id",
        emoji = "🍔",
        isIncome = isIncome
    )

    private fun dto(id: String, isIncome: Boolean = false) = CategoryDto(
        id = id,
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z",
        name = "Category $id",
        emoji = "🍔",
        isIncome = isIncome
    )

    /* ---------- getAll ---------- */

    @Test
    fun `getAll emits domain categories from local source`() = runTest {
        coEvery { localDataSource.getAll() } returns flowOf(listOf(entity("1"), entity("2")))

        val result = repository.getAll().first()

        val success = result as DomainResult.Success
        assertThat(success.data.map { it.id }).containsExactly("1", "2")
    }

    @Test
    fun `getAll launches background pull of server data`() = runTest {
        coEvery { localDataSource.getAll() } returns flowOf(emptyList())

        repository.getAll()
        appCoroutineContext.runAll()

        coVerify(exactly = 1) { syncManager.pullServerData() }
    }

    @Test
    fun `getAll emits Failure when local flow fails`() = runTest {
        val boom = RuntimeException("db")
        coEvery { localDataSource.getAll() } returns flow { throw boom }

        val result = repository.getAll().first()

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.Unknown(boom)))
    }

    /* ---------- getByType ---------- */

    @Test
    fun `getByType emits domain categories of requested type`() = runTest {
        coEvery { localDataSource.getByType(true) } returns
            flowOf(listOf(entity("1", isIncome = true)))

        val result = repository.getByType(isIncome = true).first()

        val success = result as DomainResult.Success
        assertThat(success.data).hasSize(1)
        assertThat(success.data.first().isIncome).isTrue()
    }

    @Test
    fun `getByType refreshes local cache from server in background`() = runTest {
        coEvery { localDataSource.getByType(false) } returns flowOf(emptyList())
        coEvery { remoteDataSource.getByType(false) } returns
            Response.success(listOf(dto("1"), dto("2")))
        val inserted = slot<List<CategoryEntity>>()
        coEvery { localDataSource.insert(capture(inserted)) } returns Unit

        repository.getByType(isIncome = false)
        appCoroutineContext.runAll()

        assertThat(inserted.captured.map { it.id }).containsExactly("1", "2")
    }

    @Test
    fun `getByType does not touch local cache when server call fails`() = runTest {
        coEvery { localDataSource.getByType(false) } returns flowOf(emptyList())
        coEvery { remoteDataSource.getByType(false) } throws RuntimeException("offline")

        repository.getByType(isIncome = false)
        appCoroutineContext.runAll()

        coVerify(exactly = 0) { localDataSource.insert(any()) }
    }
}
