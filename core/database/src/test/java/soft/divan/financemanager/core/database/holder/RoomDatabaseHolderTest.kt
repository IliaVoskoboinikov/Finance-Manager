package soft.divan.financemanager.core.database.holder

import android.content.Context
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import soft.divan.financemanager.core.database.entity.CurrencyEntity
import soft.divan.financemanager.core.database.seed.DefaultCategories

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RoomDatabaseHolderTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val files = DatabaseFiles(context)
    private val factory = TestDatabaseFactory(context)
    private val scheduler = StandardTestDispatcher()
    private val holder = RoomDatabaseHolder(factory, files, scheduler)

    @Test
    fun `closed database refuses operations`() = holderTest {
        assertThat(holder.state.value).isEqualTo(DatabaseState.CLOSED)

        val error = runCatching { holder.withDatabase { it.categoryDao().getById("x") } }

        assertThat(error.exceptionOrNull()).isInstanceOf(DatabaseLockedException::class.java)
    }

    @Test
    fun `open database is seeded and usable`() = holderTest {
        holder.open(key())

        val categories = holder.withDatabase { it.categoryDao().getAll().first() }

        assertThat(holder.state.value).isEqualTo(DatabaseState.OPEN)
        assertThat(categories).hasSize(DefaultCategories.all.size)
        assertThat(files.exists()).isTrue()
    }

    @Test
    fun `close zeroes the key handed to the database`() = holderTest {
        holder.open(key())

        holder.close()

        assertThat(factory.keys.single()).containsOnly(0)
        assertThat(holder.state.value).isEqualTo(DatabaseState.CLOSED)
    }

    @Test
    fun `second open keeps the database and zeroes the extra key`() = holderTest {
        holder.open(key())
        val extra = key()

        holder.open(extra)

        assertThat(extra).containsOnly(0)
        assertThat(factory.keys).hasSize(1)
    }

    @Test
    fun `failed open leaves the database closed`() = holderTest {
        factory.failNext = IllegalStateException("file is not a database")

        val error = runCatching { holder.open(key()) }.exceptionOrNull()

        assertThat(error).hasMessage("file is not a database")
        assertThat(holder.state.value).isEqualTo(DatabaseState.CLOSED)
    }

    @Test
    fun `close waits for running operations and refuses new ones`() = holderTest {
        holder.open(key())
        val release = CompletableDeferred<Unit>()
        val running = launch {
            holder.withDatabase { db ->
                release.await()
                db.currencyDao().insertCurrencies(listOf(CurrencyEntity("rub", "Рубль")))
            }
        }
        runCurrent()

        val closing = launch { holder.close() }
        runCurrent()

        assertThat(holder.state.value).isEqualTo(DatabaseState.CLOSING)
        assertThat(closing.isCompleted).isFalse()
        val refused = runCatching { holder.withDatabase { } }.exceptionOrNull()
        assertThat(refused).isInstanceOf(DatabaseLockedException::class.java)

        release.complete(Unit)
        running.join()
        closing.join()

        assertThat(holder.state.value).isEqualTo(DatabaseState.CLOSED)
    }

    @Test
    fun `nested calls reuse the outer lease even while closing`() = holderTest {
        holder.open(key())
        val inTransaction = CompletableDeferred<Unit>()
        val proceed = CompletableDeferred<Unit>()

        val transaction = async {
            holder.withDatabase { db ->
                db.withTransaction {
                    inTransaction.complete(Unit)
                    proceed.await()
                    // Как репозиторий внутри транзакции: новая аренда была бы отклонена
                    holder.withDatabase {
                        it.currencyDao().insertCurrencies(listOf(CurrencyEntity("usd", "Доллар")))
                    }
                    holder.withDatabase { it.currencyDao().getCurrencyById("usd") }
                }
            }
        }
        inTransaction.await()
        val closing = launch { holder.close() }
        runCurrent()
        assertThat(holder.state.value).isEqualTo(DatabaseState.CLOSING)

        proceed.complete(Unit)

        assertThat(transaction.await()?.name).isEqualTo("Доллар")
        closing.join()
        assertThat(holder.state.value).isEqualTo(DatabaseState.CLOSED)
    }

    @Test
    fun `lifecycle cannot be changed from inside a lease`() = holderTest {
        holder.open(key())

        val close = holder.withDatabase { runCatching { holder.close() }.exceptionOrNull() }
        val wipe = holder.withDatabase { runCatching { holder.wipe() }.exceptionOrNull() }
        val open = holder.withDatabase { runCatching { holder.open(key()) }.exceptionOrNull() }

        assertThat(close).isInstanceOf(IllegalStateException::class.java)
        assertThat(wipe).isInstanceOf(IllegalStateException::class.java)
        assertThat(open).isInstanceOf(IllegalStateException::class.java)
        assertThat(holder.state.value).isEqualTo(DatabaseState.OPEN)
    }

    @Test
    fun `lease captured by a detached coroutine is not reused after it ended`() = holderTest {
        holder.open(key())
        val leakedContext = holder.withDatabase { currentCoroutineContext() }

        holder.close()
        val error = runCatching {
            CoroutineScope(leakedContext.minusKey(Job)).async { holder.withDatabase { } }.await()
        }.exceptionOrNull()

        assertThat(error).isInstanceOf(DatabaseLockedException::class.java)
    }

    @Test
    fun `observe is silent while closed and follows reopening`() = holderTest {
        val emissions = Channel<Int>(Channel.UNLIMITED)
        val observer = launch {
            holder.observe { it.categoryDao().getAll() }.collect { emissions.send(it.size) }
        }
        runCurrent()
        assertThat(emissions.tryReceive().getOrNull()).isNull()

        holder.open(key())
        assertThat(emissions.receive()).isEqualTo(DefaultCategories.all.size)

        holder.wipe()
        holder.open(key())
        assertThat(emissions.receive()).isEqualTo(DefaultCategories.all.size)

        observer.cancel()
    }

    @Test
    fun `observe stays silent after close`() = holderTest {
        holder.open(key())
        val first = holder.observe { it.currencyDao().getAllCurrencies() }.first()

        holder.close()
        val silent = runCatching {
            withTimeout(SILENCE_TIMEOUT_MS) {
                holder.observe { it.currencyDao().getAllCurrencies() }.take(1).toList()
            }
        }

        assertThat(first).isEmpty()
        assertThat(silent.isFailure).isTrue()
    }

    @Test
    fun `wipe removes the files and the next open seeds a fresh database`() = holderTest {
        holder.open(key())
        holder.withDatabase { it.currencyDao().insertCurrencies(listOf(CurrencyEntity("rub", "Рубль"))) }

        holder.wipe()

        assertThat(files.exists()).isFalse()
        assertThat(holder.state.value).isEqualTo(DatabaseState.CLOSED)

        holder.open(key())
        assertThat(holder.withDatabase { it.currencyDao().getCurrencyById("rub") }).isNull()
        assertThat(holder.withDatabase { it.categoryDao().getAll().first() })
            .hasSize(DefaultCategories.all.size)
    }

    @Test
    fun `reopening keeps the data and does not seed twice`() = holderTest {
        holder.open(key())
        holder.withDatabase { it.currencyDao().insertCurrencies(listOf(CurrencyEntity("rub", "Рубль"))) }
        holder.close()

        holder.open(key())

        assertThat(holder.withDatabase { it.currencyDao().getCurrencyById("rub") }).isNotNull()
        assertThat(holder.withDatabase { it.categoryDao().getAll().first() })
            .hasSize(DefaultCategories.all.size)
    }

    @Test
    fun `failed close still ends the instance so the database can be reopened`() = holderTest {
        factory.failClose = IllegalStateException("close failed")
        holder.open(key())

        val closed = runCatching { holder.close() }

        assertThat(closed.exceptionOrNull()).isInstanceOf(IllegalStateException::class.java)
        // Не «CLOSING навсегда»: иначе следующее открытие сочло бы базу уже открытой
        assertThat(holder.state.value).isEqualTo(DatabaseState.CLOSED)
        assertThat(factory.keys.single()).containsOnly(0)
        holder.open(key())
        assertThat(holder.withDatabase { it.categoryDao().getAll().first() })
            .hasSize(DefaultCategories.all.size)
    }

    @Test
    fun `wipe deletes the files even if closing failed`() = holderTest {
        factory.failClose = IllegalStateException("close failed")
        holder.open(key())

        runCatching { holder.wipe() }

        assertThat(files.exists()).isFalse()
        assertThat(holder.state.value).isEqualTo(DatabaseState.CLOSED)
    }

    @Test
    fun `close of a closed database does nothing`() = holderTest {
        holder.close()
        holder.wipe()

        assertThat(holder.state.value).isEqualTo(DatabaseState.CLOSED)
    }

    /** Тест с гарантированным закрытием базы: иначе Room держал бы соединения между тестами. */
    private fun holderTest(body: suspend TestScope.() -> Unit) = runTest(scheduler) {
        try {
            body()
        } finally {
            holder.close()
            files.delete()
        }
    }

    private fun key() = ByteArray(RawKey.KEY_SIZE) { 1 }

    private companion object {
        const val SILENCE_TIMEOUT_MS = 1_000L
    }
}
