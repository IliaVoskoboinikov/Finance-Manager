package soft.divan.financemanager.core.database.dao

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import soft.divan.financemanager.core.database.entity.AccountEntity
import soft.divan.financemanager.core.database.model.SyncStatus

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AccountDaoTest : RoomDaoTest() {

    private val dao get() = db.accountDao()

    private fun entity(
        localId: String = "local-1",
        serverId: String? = "server-1",
        name: String = "Cash",
        syncStatus: SyncStatus = SyncStatus.SYNCED,
        archived: Boolean = false
    ) = AccountEntity(
        localId = localId,
        serverId = serverId,
        name = name,
        balance = "100.50",
        currencyId = "rub-id",
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z",
        syncStatus = syncStatus,
        archived = archived
    )

    @Test
    fun `insert and read by local id`() = runTest {
        dao.insert(entity())

        assertThat(dao.getByLocalId("local-1")).isEqualTo(entity())
        assertThat(dao.getByLocalId("missing")).isNull()
    }

    @Test
    fun `insert persists archived flag`() = runTest {
        dao.insert(entity(archived = true))

        assertThat(dao.getByLocalId("local-1")!!.archived).isTrue()
    }

    @Test
    fun `insert replaces row with the same local id`() = runTest {
        dao.insert(entity(name = "Old"))
        dao.insert(entity(name = "New"))

        assertThat(dao.getAll().first()).hasSize(1)
        assertThat(dao.getByLocalId("local-1")!!.name).isEqualTo("New")
    }

    @Test
    fun `getAll emits every stored account`() = runTest {
        dao.insert(entity(localId = "a1", serverId = "s1"))
        dao.insert(entity(localId = "a2", serverId = "s2"))

        assertThat(dao.getAll().first().map { it.localId }).containsExactly("a1", "a2")
    }

    @Test
    fun `getByServerId finds account and returns null for unknown`() = runTest {
        dao.insert(entity())

        assertThat(dao.getByServerId("server-1")!!.localId).isEqualTo("local-1")
        assertThat(dao.getByServerId("nope")).isNull()
    }

    @Test
    fun `getByServerIds returns only requested accounts`() = runTest {
        dao.insert(entity(localId = "a1", serverId = "s1"))
        dao.insert(entity(localId = "a2", serverId = "s2"))
        dao.insert(entity(localId = "a3", serverId = "s3"))

        val found = dao.getByServerIds(listOf("s1", "s3", "ghost"))

        assertThat(found.map { it.localId }).containsExactlyInAnyOrder("a1", "a3")
    }

    @Test
    fun `getPendingSync returns everything except SYNCED`() = runTest {
        dao.insert(entity(localId = "a1", syncStatus = SyncStatus.SYNCED))
        dao.insert(entity(localId = "a2", serverId = null, syncStatus = SyncStatus.PENDING_CREATE))
        dao.insert(entity(localId = "a3", syncStatus = SyncStatus.PENDING_UPDATE))
        dao.insert(entity(localId = "a4", syncStatus = SyncStatus.PENDING_DELETE))

        val pending = dao.getPendingSync()

        assertThat(pending.map { it.localId }).containsExactlyInAnyOrder("a2", "a3", "a4")
    }

    @Test
    fun `update rewrites stored account`() = runTest {
        dao.insert(entity())

        dao.update(entity(name = "Renamed", syncStatus = SyncStatus.PENDING_UPDATE))

        val stored = dao.getByLocalId("local-1")!!
        assertThat(stored.name).isEqualTo("Renamed")
        assertThat(stored.syncStatus).isEqualTo(SyncStatus.PENDING_UPDATE)
    }

    @Test
    fun `delete removes only requested account`() = runTest {
        dao.insert(entity(localId = "a1"))
        dao.insert(entity(localId = "a2", serverId = "s2"))

        dao.delete("a1")

        assertThat(dao.getAll().first().map { it.localId }).containsExactly("a2")
    }

    @Test
    fun `deleteAll clears the table`() = runTest {
        dao.insert(entity(localId = "a1"))
        dao.insert(entity(localId = "a2", serverId = "s2"))

        dao.deleteAll()

        assertThat(dao.getAll().first()).isEmpty()
    }
}
