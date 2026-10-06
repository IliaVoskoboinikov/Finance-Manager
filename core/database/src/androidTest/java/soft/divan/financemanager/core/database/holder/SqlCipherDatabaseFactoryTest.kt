package soft.divan.financemanager.core.database.holder

import android.content.Context
import android.os.SystemClock
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import soft.divan.financemanager.core.database.entity.CurrencyEntity
import soft.divan.financemanager.core.database.entity.TransactionEntity
import soft.divan.financemanager.core.database.model.SyncStatus
import java.time.Instant
import java.time.ZoneId

/**
 * Настоящий SQLCipher на устройстве: в JVM его нативная библиотека не загружается, поэтому
 * шифрование файла проверяется только здесь.
 */
@RunWith(AndroidJUnit4::class)
class SqlCipherDatabaseFactoryTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val files = DatabaseFiles(context)
    private val factory = SqlCipherDatabaseFactory(context)

    @Before
    @After
    fun clean() {
        files.delete()
    }

    @Test
    fun newDatabaseIsEncryptedAndSeeded() = runBlocking {
        val opened = factory.open(key(1))
        val categories = opened.database.categoryDao().getAll().first()
        opened.close()

        assertEquals(SEEDED_CATEGORIES, categories.size)
        assertTrue(files.exists())
        // Первые 16 байт зашифрованного файла — случайная соль, а не заголовок SQLite
        assertFalse(files.isPlaintext())
    }

    @Test
    fun sameKeyReopensTheData() = runBlocking {
        factory.open(key(1)).also { opened ->
            opened.database.currencyDao().insertCurrencies(listOf(CurrencyEntity("rub", "Рубль")))
            opened.close()
        }

        val reopened = factory.open(key(1))
        val currency = reopened.database.currencyDao().getCurrencyById("rub")
        reopened.close()

        assertNotNull(currency)
    }

    @Test
    fun wrongKeyIsRejectedOnOpen() = runBlocking {
        factory.open(key(1)).close()

        try {
            factory.open(key(2)).close()
            fail("Database opened with a wrong key")
        } catch (expected: RuntimeException) {
            Log.i(TAG, "Wrong key rejected: ${expected.javaClass.simpleName}")
        }

        // Неудачная попытка файл не портит — верный ключ по-прежнему подходит
        factory.open(key(1)).close()
    }

    @Test
    fun keyArrayIsWipedRightAfterOpening() {
        val key = key(7)

        val opened = factory.open(key)

        assertArrayEquals(ByteArray(RawKey.KEY_SIZE), key)
        opened.close()
    }

    @Test
    fun localtimeModifierUsesTheSystemZone() {
        // Выборка транзакций за период опирается на date(…, 'localtime'), а SQLite здесь свой,
        // не системный: без часового пояса модификатор вернул бы NULL или дату по UTC
        val instant = Instant.parse(LATE_EVENING_UTC)
        val expected = instant.atZone(ZoneId.systemDefault()).toLocalDate().toString()

        val opened = factory.open(key(1))
        val actual = opened.database.openHelper.readableDatabase
            .query("SELECT date('$LATE_EVENING_UTC', 'localtime')")
            .use { cursor ->
                cursor.moveToFirst()
                cursor.getString(0)
            }
        opened.close()

        Log.i(TAG, "date(localtime) in ${ZoneId.systemDefault()}: $actual")
        assertEquals(expected, actual)
    }

    @Test
    fun periodQueryFindsTransactionsOfTheLastDay() = runBlocking {
        val opened = factory.open(key(1))
        val dao = opened.database.transactionDao()
        dao.insert(transaction(date = LATE_EVENING_UTC))

        val month = dao.getByAccountAndPeriod(ACCOUNT, "2026-09-01", "2026-09-24").first()
        val day = dao.getByAccountAndPeriod(ACCOUNT, "2026-09-24", "2026-09-24").first()
        opened.close()

        Log.i(TAG, "Period query: month=${month.size}, day=${day.size}")
        assertEquals(1, day.size)
        assertEquals(1, month.size)
    }

    @Test
    fun rawKeyOpeningIsFast() {
        factory.open(key(1)).close()

        val started = SystemClock.elapsedRealtime()
        val opened = factory.open(key(1))
        val elapsed = SystemClock.elapsedRealtime() - started
        opened.close()

        Log.i(TAG, "Raw key open took $elapsed ms")
        // Готовый ключ минует PBKDF2 SQLCipher: открытие — десятки миллисекунд, а не сотни
        assertTrue("Opening took $elapsed ms", elapsed < RAW_KEY_OPEN_BUDGET_MS)
    }

    private fun key(seed: Int) = ByteArray(RawKey.KEY_SIZE) { (it * seed + seed).toByte() }

    private fun transaction(date: String) = TransactionEntity(
        localId = "t1",
        serverId = null,
        accountLocalId = ACCOUNT,
        type = "EXPENSE",
        targetAccountLocalId = null,
        accountServerId = null,
        categoryId = "c1",
        currencyId = "RUB",
        amount = "2",
        transactionDate = date,
        comment = "",
        createdAt = date,
        updatedAt = date,
        syncStatus = SyncStatus.SYNCED
    )

    private companion object {
        const val TAG = "SqlCipherTest"
        const val SEEDED_CATEGORIES = 24
        const val RAW_KEY_OPEN_BUDGET_MS = 1_000L

        /** Формат `TimeMapper.toApi`; в поясах восточнее UTC это уже следующий день. */
        const val LATE_EVENING_UTC = "2026-09-23T22:30:00Z"
        const val ACCOUNT = "a1"
    }
}
