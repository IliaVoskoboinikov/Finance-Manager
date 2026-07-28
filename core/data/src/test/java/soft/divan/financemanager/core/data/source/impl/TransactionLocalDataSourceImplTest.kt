package soft.divan.financemanager.core.data.source.impl

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.database.dao.TransactionDao
import soft.divan.financemanager.core.database.entity.TransactionEntity
import soft.divan.financemanager.core.database.model.SyncStatus

class TransactionLocalDataSourceImplTest {

    private val dao = mockk<TransactionDao>(relaxUnitFun = true)
    private val dataSource = TransactionLocalDataSourceImpl(dao)

    private val entity = TransactionEntity(
        localId = "local-t1",
        serverId = "server-t1",
        accountLocalId = "local-a1",
        type = "EXPENSE",
        targetAccountLocalId = null,
        accountServerId = "server-a1",
        categoryId = "cat-1",
        currencyId = "rub-id",
        amount = "42.42",
        transactionDate = "2024-01-15T10:00:00Z",
        comment = "lunch",
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z",
        syncStatus = SyncStatus.SYNCED
    )

    @Test
    fun `insert delegates to dao`() = runTest {
        dataSource.insert(entity)

        coVerify(exactly = 1) { dao.insert(entity) }
    }

    @Test
    fun `getByAccountAndPeriod delegates to dao`() = runTest {
        val flow = flowOf(listOf(entity))
        every {
            dao.getByAccountAndPeriod("local-a1", "2024-01-01", "2024-01-31")
        } returns flow

        assertThat(
            dataSource.getByAccountAndPeriod("local-a1", "2024-01-01", "2024-01-31")
        ).isSameAs(flow)
    }

    @Test
    fun `getByLocalId delegates to dao`() = runTest {
        coEvery { dao.getByLocalId("local-t1") } returns entity

        assertThat(dataSource.getByLocalId("local-t1")).isEqualTo(entity)
    }

    @Test
    fun `getByServerId delegates to dao`() = runTest {
        coEvery { dao.getByServerId("server-t1") } returns entity

        assertThat(dataSource.getByServerId("server-t1")).isEqualTo(entity)
    }

    @Test
    fun `getByServerIds delegates to dao`() = runTest {
        coEvery { dao.getByServerIds(listOf("server-t1")) } returns listOf(entity)

        assertThat(dataSource.getByServerIds(listOf("server-t1"))).containsExactly(entity)
    }

    @Test
    fun `getByAccountId delegates to dao`() = runTest {
        coEvery { dao.getByAccountId("local-a1") } returns listOf(entity)

        assertThat(dataSource.getByAccountId("local-a1")).containsExactly(entity)
    }

    @Test
    fun `getPendingSync delegates to dao`() = runTest {
        coEvery { dao.getPendingSync() } returns listOf(entity)

        assertThat(dataSource.getPendingSync()).containsExactly(entity)
    }

    @Test
    fun `update delegates to dao`() = runTest {
        dataSource.update(entity)

        coVerify(exactly = 1) { dao.update(entity) }
    }

    @Test
    fun `delete delegates to dao`() = runTest {
        dataSource.delete("local-t1")

        coVerify(exactly = 1) { dao.delete("local-t1") }
    }

    @Test
    fun `deleteAll delegates to dao`() = runTest {
        dataSource.deleteAll()

        coVerify(exactly = 1) { dao.deleteAll() }
    }
}
