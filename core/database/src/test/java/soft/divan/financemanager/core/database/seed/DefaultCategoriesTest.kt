package soft.divan.financemanager.core.database.seed

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import soft.divan.financemanager.core.database.db.FinanceManagerDatabase
import soft.divan.financemanager.core.database.db.withAppDefaults
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DefaultCategoriesTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `reference set has 24 categories with server ids`() {
        val categories = DefaultCategories.all

        assertThat(categories).hasSize(24)
        assertThat(categories.map { it.id }).doesNotHaveDuplicates()
        // Идентификаторы — серверные GUID: синк залогиненного пользователя обновит эти же строки
        categories.forEach { UUID.fromString(it.id) }
        assertThat(categories.count { it.isIncome }).isEqualTo(6)
        assertThat(categories.count { !it.isIncome }).isEqualTo(18)
    }

    @Test
    fun `seeded rows are older than anything the server sends`() {
        DefaultCategories.all.forEach {
            assertThat(it.createdAt).isEqualTo(DefaultCategories.SEEDED_AT)
            assertThat(it.updatedAt).isEqualTo(DefaultCategories.SEEDED_AT)
        }
    }

    @Test
    fun `new database is created with the reference set`() = runTest {
        val db = Room.inMemoryDatabaseBuilder(context, FinanceManagerDatabase::class.java)
            .withAppDefaults()
            .build()

        val stored = db.categoryDao().getAll().first()
        db.close()

        assertThat(stored).containsExactlyInAnyOrderElementsOf(DefaultCategories.all)
    }

    @Test
    fun `server version of a seeded category replaces it instead of duplicating`() = runTest {
        val db = Room.inMemoryDatabaseBuilder(context, FinanceManagerDatabase::class.java)
            .withAppDefaults()
            .build()
        val seeded = DefaultCategories.all.first()

        val fromServer = seeded.copy(name = "С сервера", updatedAt = "2026-01-01T00:00:00Z")
        db.categoryDao().insertAll(listOf(fromServer))

        val stored = db.categoryDao().getAll().first()
        db.close()
        assertThat(stored).hasSize(DefaultCategories.all.size)
        assertThat(stored.single { it.id == seeded.id }.name).isEqualTo("С сервера")
    }
}
