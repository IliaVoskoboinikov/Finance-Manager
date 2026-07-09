package soft.divan.financemanager.core.data

import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import soft.divan.financemanager.core.data.util.coroutne.AppCoroutineContext
import soft.divan.financemanager.core.database.db.FinanceManagerDatabase
import soft.divan.financemanager.core.domain.error.DomainError
import soft.divan.financemanager.core.domain.result.DomainResult
import kotlin.coroutines.EmptyCoroutineContext

@RunWith(RobolectricTestRunner::class)
class RoomTransactionRunnerTest {

    /**
     * Записывает запуски вместо реального выполнения: тест сам решает, когда выполнить
     * отложенные действия. launchSync повторяет логику DefaultAppCoroutineContext.
     */
    private class RecordingAppCoroutineContext : AppCoroutineContext {
        val launched = mutableListOf<suspend () -> Unit>()

        override fun launch(block: suspend CoroutineScope.() -> Unit) {
            launched.add { CoroutineScope(EmptyCoroutineContext).block() }
        }

        override suspend fun launchSync(block: suspend () -> Unit) {
            val queue = currentCoroutineContext()[PostCommitSyncQueue]
            if (queue != null) queue.add(block) else launched.add(block)
        }
    }

    private lateinit var db: FinanceManagerDatabase
    private val appContext = RecordingAppCoroutineContext()
    private lateinit var runner: RoomTransactionRunner

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            FinanceManagerDatabase::class.java
        ).allowMainThreadQueries().build()
        runner = RoomTransactionRunner(db, appContext)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `sync scheduled inside a transaction runs only after commit`() = runTest {
        var syncExecuted = false

        val result = runner.runInTransaction {
            appContext.launchSync { syncExecuted = true }
            // Внутри транзакции пуш ещё не должен быть запущен.
            assertThat(appContext.launched).isEmpty()
            DomainResult.Success(Unit)
        }

        assertThat(result).isEqualTo(DomainResult.Success(Unit))
        // После commit действие передано на запуск; выполняем и проверяем эффект.
        assertThat(appContext.launched).hasSize(1)
        assertThat(syncExecuted).isFalse()
        appContext.launched.single().invoke()
        assertThat(syncExecuted).isTrue()
    }

    @Test
    fun `sync scheduled inside a rolled back transaction is discarded`() = runTest {
        var syncExecuted = false

        val result = runner.runInTransaction<DomainResult<Unit>> {
            appContext.launchSync { syncExecuted = true }
            DomainResult.Failure(DomainError.Unknown(null)).rollbackOnError()
        }

        assertThat(result).isEqualTo(DomainResult.Failure(DomainError.Unknown(null)))
        // Rollback: отложенный пуш отброшен и никогда не запустится.
        assertThat(appContext.launched).isEmpty()
        assertThat(syncExecuted).isFalse()
    }

    @Test
    fun `multiple syncs are dispatched in order after commit`() = runTest {
        val executed = mutableListOf<String>()

        runner.runInTransaction {
            appContext.launchSync { executed.add("first") }
            appContext.launchSync { executed.add("second") }
            DomainResult.Success(Unit)
        }

        assertThat(appContext.launched).hasSize(2)
        appContext.launched.forEach { it.invoke() }
        assertThat(executed).containsExactly("first", "second")
    }

    @Test
    fun `launchSync outside a transaction runs immediately`() = runTest {
        appContext.launchSync { }

        assertThat(appContext.launched).hasSize(1)
    }
}
