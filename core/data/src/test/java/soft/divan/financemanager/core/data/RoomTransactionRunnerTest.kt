package soft.divan.financemanager.core.data

import androidx.room.withTransaction
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import soft.divan.financemanager.core.database.db.FinanceManagerDatabase
import soft.divan.financemanager.core.domain.error.DomainError
import soft.divan.financemanager.core.domain.result.DomainResult

class RoomTransactionRunnerTest {

    private val db = mockk<FinanceManagerDatabase>()
    private val runner = RoomTransactionRunner(db)

    @Before
    fun setUp() {
        mockkStatic("androidx.room.RoomDatabaseKt")
        coEvery { db.withTransaction(any<suspend () -> Any?>()) } coAnswers {
            // arg(0) — receiver расширения (сама БД), arg(1) — переданный block
            secondArg<suspend () -> Any?>().invoke()
        }
    }

    @After
    fun tearDown() {
        unmockkStatic("androidx.room.RoomDatabaseKt")
    }

    @Test
    fun `runInTransaction returns block result`() = runTest {
        val result = runner.runInTransaction { "done" }

        assertThat(result).isEqualTo("done")
    }

    @Test
    fun `runInTransaction converts rollback exception to Failure`() = runTest {
        val failed: DomainResult<Unit> = DomainResult.Failure(DomainError.NoData)

        val result = runner.runInTransaction<DomainResult<Unit>> {
            failed.rollbackOnError()
            DomainResult.Success(Unit)
        }

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.NoData))
    }

    @Test
    fun `runInTransaction propagates unexpected exceptions`() = runTest {
        val boom = IllegalStateException("boom")

        val thrown = runCatching {
            runner.runInTransaction<Unit> { throw boom }
        }.exceptionOrNull()

        assertThat(thrown).isSameAs(boom)
    }
}
