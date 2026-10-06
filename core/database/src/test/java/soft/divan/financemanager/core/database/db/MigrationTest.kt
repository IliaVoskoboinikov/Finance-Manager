package soft.divan.financemanager.core.database.db

import android.content.Context
import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import soft.divan.financemanager.core.database.seed.DefaultCategories

/**
 * Миграции схемы на выгруженных JSON-схемах (`core/database/schemas`).
 *
 * Базовая версия — 8. Когда появится версия 9, здесь добавляется тест `migrate8To9`:
 * `createDatabase(…, 8)` → наполнить строками → `runMigrationsAndValidate(…, 9, true, …)` →
 * проверить, что строки пережили миграцию.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MigrationTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        FinanceManagerDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun `exported schema matches the entities`() {
        helper.createDatabase(TEST_DB, BASE_VERSION).close()

        // Валидация сравнивает таблицы, созданные по JSON, с тем, что ждут сущности: расхождение
        // значит, что схему поменяли без новой версии
        helper.runMigrationsAndValidate(
            TEST_DB,
            BASE_VERSION,
            true,
            *DatabaseMigrations.ALL.toTypedArray()
        ).close()
    }

    @Test
    fun `database from the base schema opens with the current code and keeps its rows`() = runTest {
        helper.createDatabase(TEST_DB, BASE_VERSION).use { db ->
            db.execSQL("INSERT INTO currency (id, name) VALUES ('rub', 'Рубль')")
        }

        val room = Room.databaseBuilder(context, FinanceManagerDatabase::class.java, TEST_DB)
            .withAppDefaults()
            .build()
        val currency = room.currencyDao().getCurrencyById("rub")
        // Файл создан не Room'ом — onCreate не вызывался, засева нет: миграции данные не трогают
        val categories = room.categoryDao().getAll().first()
        room.close()

        assertThat(currency?.name).isEqualTo("Рубль")
        assertThat(categories).isEmpty()
    }

    @Test
    fun `current schema version is the base version plus migrations`() {
        val db = Room.inMemoryDatabaseBuilder(context, FinanceManagerDatabase::class.java)
            .withAppDefaults()
            .build()

        val version = db.openHelper.readableDatabase.version
        db.close()

        assertThat(version).isEqualTo(BASE_VERSION + DatabaseMigrations.ALL.size)
        assertThat(DefaultCategories.all).isNotEmpty()
    }

    private companion object {
        const val TEST_DB = "migration-test.db"
        const val BASE_VERSION = 8
    }
}
