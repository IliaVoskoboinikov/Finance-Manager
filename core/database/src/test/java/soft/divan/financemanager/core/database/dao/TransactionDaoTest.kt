package soft.divan.financemanager.core.database.dao

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import soft.divan.financemanager.core.database.entity.TransactionEntity
import soft.divan.financemanager.core.database.model.SyncStatus
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TransactionDaoTest : RoomDaoTest() {

    private val dao get() = db.transactionDao()

    /**
     * transactionDate хранится ISO-строкой в UTC, а запрос сравнивает
     * date(transactionDate, 'localtime') — поэтому даты строим от локального
     * полудня, чтобы ожидания не зависели от таймзоны машины.
     */
    private fun instantAtLocalNoon(date: LocalDate): String =
        date.atTime(LocalTime.NOON).atZone(ZoneId.systemDefault()).toInstant().toString()

    private fun entity(
        localId: String,
        accountLocalId: String = "acc-1",
        date: LocalDate = LocalDate.of(2024, 1, 15),
        serverId: String? = null,
        syncStatus: SyncStatus = SyncStatus.SYNCED
    ) = TransactionEntity(
        localId = localId,
        serverId = serverId,
        accountLocalId = accountLocalId,
        type = "EXPENSE",
        targetAccountLocalId = null,
        accountServerId = null,
        categoryId = "cat-1",
        currencyId = "rub-id",
        amount = "42.42",
        transactionDate = instantAtLocalNoon(date),
        comment = "",
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z",
        syncStatus = syncStatus
    )

    /* ---------- getByAccountAndPeriod ---------- */

    @Test
    fun `period query includes both boundary dates`() = runTest {
        dao.insert(entity("before", date = LocalDate.of(2024, 1, 9)))
        dao.insert(entity("start", date = LocalDate.of(2024, 1, 10)))
        dao.insert(entity("middle", date = LocalDate.of(2024, 1, 15)))
        dao.insert(entity("end", date = LocalDate.of(2024, 1, 20)))
        dao.insert(entity("after", date = LocalDate.of(2024, 1, 21)))

        val found = dao.getByAccountAndPeriod("acc-1", "2024-01-10", "2024-01-20").first()

        assertThat(found.map { it.localId }).containsExactly("start", "middle", "end")
    }

    @Test
    fun `period query filters by account`() = runTest {
        dao.insert(entity("mine", accountLocalId = "acc-1"))
        dao.insert(entity("other", accountLocalId = "acc-2"))

        val found = dao.getByAccountAndPeriod("acc-1", "2024-01-01", "2024-01-31").first()

        assertThat(found.map { it.localId }).containsExactly("mine")
    }

    @Test
    fun `period query sorts by transaction date ascending`() = runTest {
        dao.insert(entity("late", date = LocalDate.of(2024, 1, 20)))
        dao.insert(entity("early", date = LocalDate.of(2024, 1, 5)))
        dao.insert(entity("middle", date = LocalDate.of(2024, 1, 10)))

        val found = dao.getByAccountAndPeriod("acc-1", "2024-01-01", "2024-01-31").first()

        assertThat(found.map { it.localId }).containsExactly("early", "middle", "late")
    }

    @Test
    fun `single-day period returns only that day`() = runTest {
        dao.insert(entity("target", date = LocalDate.of(2024, 1, 15)))
        dao.insert(entity("eve", date = LocalDate.of(2024, 1, 14)))
        dao.insert(entity("next", date = LocalDate.of(2024, 1, 16)))

        val found = dao.getByAccountAndPeriod("acc-1", "2024-01-15", "2024-01-15").first()

        assertThat(found.map { it.localId }).containsExactly("target")
    }

    /* ---------- lookups ---------- */

    @Test
    fun `getByLocalId and getByServerId return row or null`() = runTest {
        dao.insert(entity("t1", serverId = "s1"))

        assertThat(dao.getByLocalId("t1")).isNotNull()
        assertThat(dao.getByLocalId("missing")).isNull()
        assertThat(dao.getByServerId("s1")!!.localId).isEqualTo("t1")
        assertThat(dao.getByServerId("missing")).isNull()
    }

    @Test
    fun `getByServerIds returns only requested transactions`() = runTest {
        dao.insert(entity("t1", serverId = "s1"))
        dao.insert(entity("t2", serverId = "s2"))
        dao.insert(entity("t3", serverId = "s3"))

        val found = dao.getByServerIds(listOf("s1", "s3", "ghost"))

        assertThat(found.map { it.localId }).containsExactlyInAnyOrder("t1", "t3")
    }

    @Test
    fun `getByAccountId returns account transactions newest first`() = runTest {
        dao.insert(entity("old", date = LocalDate.of(2024, 1, 5)))
        dao.insert(entity("new", date = LocalDate.of(2024, 1, 20)))
        dao.insert(entity("foreign", accountLocalId = "acc-2"))

        val found = dao.getByAccountId("acc-1")

        assertThat(found.map { it.localId }).containsExactly("new", "old")
    }

    @Test
    fun `getPendingSync returns everything except SYNCED`() = runTest {
        dao.insert(entity("t1", syncStatus = SyncStatus.SYNCED))
        dao.insert(entity("t2", syncStatus = SyncStatus.PENDING_CREATE))
        dao.insert(entity("t3", syncStatus = SyncStatus.PENDING_DELETE))

        assertThat(dao.getPendingSync().map { it.localId })
            .containsExactlyInAnyOrder("t2", "t3")
    }

    /* ---------- mutations ---------- */

    @Test
    fun `insert replaces row with the same local id`() = runTest {
        dao.insert(entity("t1"))
        dao.insert(entity("t1").copy(amount = "99.99"))

        assertThat(dao.getByAccountId("acc-1")).hasSize(1)
        assertThat(dao.getByLocalId("t1")!!.amount).isEqualTo("99.99")
    }

    @Test
    fun `update rewrites stored transaction`() = runTest {
        dao.insert(entity("t1"))

        dao.update(entity("t1").copy(comment = "updated", syncStatus = SyncStatus.PENDING_UPDATE))

        val stored = dao.getByLocalId("t1")!!
        assertThat(stored.comment).isEqualTo("updated")
        assertThat(stored.syncStatus).isEqualTo(SyncStatus.PENDING_UPDATE)
    }

    @Test
    fun `delete and deleteAll remove rows`() = runTest {
        dao.insert(entity("t1"))
        dao.insert(entity("t2"))

        dao.delete("t1")
        assertThat(dao.getByAccountId("acc-1").map { it.localId }).containsExactly("t2")

        dao.deleteAll()
        assertThat(dao.getByAccountId("acc-1")).isEmpty()
    }
}
