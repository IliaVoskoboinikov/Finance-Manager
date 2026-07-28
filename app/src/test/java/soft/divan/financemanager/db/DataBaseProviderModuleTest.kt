package soft.divan.financemanager.db

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import soft.divan.financemanager.core.database.db.FinanceManagerDatabase
import soft.divan.financemanager.core.database.di.DataBaseProviderModule
import soft.divan.financemanager.core.database.entity.CurrencyEntity

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DataBaseProviderModuleTest {

    private lateinit var db: FinanceManagerDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = DataBaseProviderModule.provideDatabase(context)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `database opens from prepackaged asset and is usable`() = runTest {
        // Открытие копирует ассет и выполняет destructive-миграцию v1→v2 — не должно падать
        val categories = db.categoryDao().getAll().first()

        assertThat(categories).isNotNull()
    }

    @Test
    fun `database schema version is above prepackaged asset version`() {
        assertThat(db.openHelper.readableDatabase.version).isGreaterThan(1)
    }

    @Test
    fun `all daos are wired and writable`() = runTest {
        db.currencyDao().insertCurrencies(listOf(CurrencyEntity(id = "rub", name = "Рубль")))

        assertThat(db.currencyDao().getCurrencyById("rub")).isNotNull()
        assertThat(db.accountDao().getPendingSync()).isEmpty()
        assertThat(db.transactionDao().getPendingSync()).isEmpty()
    }

    @Test
    fun `dao providers expose database daos and cleanup manager works`() = runTest {
        assertThat(DataBaseProviderModule.provideTransactionDao(db)).isNotNull()
        assertThat(DataBaseProviderModule.provideCategoryDao(db)).isNotNull()
        assertThat(DataBaseProviderModule.provideAccountDao(db)).isNotNull()
        assertThat(DataBaseProviderModule.provideCurrencyDao(db)).isNotNull()

        val cleanup = DataBaseProviderModule.provideDatabaseCleanupManager(
            accountDao = db.accountDao(),
            transactionDao = db.transactionDao()
        )
        cleanup.clearUserData()

        assertThat(db.accountDao().getAll().first()).isEmpty()
    }
}
