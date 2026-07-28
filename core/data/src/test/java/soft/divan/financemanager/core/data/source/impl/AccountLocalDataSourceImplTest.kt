package soft.divan.financemanager.core.data.source.impl

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.database.dao.AccountDao
import soft.divan.financemanager.core.database.entity.AccountEntity
import soft.divan.financemanager.core.database.model.SyncStatus

class AccountLocalDataSourceImplTest {

    private val dao = mockk<AccountDao>(relaxUnitFun = true)
    private val dataSource = AccountLocalDataSourceImpl(dao)

    private val entity = AccountEntity(
        localId = "local-1",
        serverId = "server-1",
        name = "Cash",
        balance = "10",
        currencyId = "rub-id",
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z",
        syncStatus = SyncStatus.SYNCED
    )

    @Test
    fun `create delegates to dao insert`() = runTest {
        dataSource.create(entity)

        coVerify(exactly = 1) { dao.insert(entity) }
    }

    @Test
    fun `getAll delegates to dao`() = runTest {
        val flow = flowOf(listOf(entity))
        every { dao.getAll() } returns flow

        assertThat(dataSource.getAll()).isSameAs(flow)
    }

    @Test
    fun `getByLocalId delegates to dao`() = runTest {
        coEvery { dao.getByLocalId("local-1") } returns entity

        assertThat(dataSource.getByLocalId("local-1")).isEqualTo(entity)
    }

    @Test
    fun `getByServerId delegates to dao`() = runTest {
        coEvery { dao.getByServerId("server-1") } returns entity

        assertThat(dataSource.getByServerId("server-1")).isEqualTo(entity)
    }

    @Test
    fun `getByServerIds delegates to dao`() = runTest {
        coEvery { dao.getByServerIds(listOf("server-1")) } returns listOf(entity)

        assertThat(dataSource.getByServerIds(listOf("server-1"))).containsExactly(entity)
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
        dataSource.delete("local-1")

        coVerify(exactly = 1) { dao.delete("local-1") }
    }

    @Test
    fun `deleteAll delegates to dao`() = runTest {
        dataSource.deleteAll()

        coVerify(exactly = 1) { dao.deleteAll() }
    }
}
