package soft.divan.financemanager.core.database.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Before
import soft.divan.financemanager.core.database.db.FinanceManagerDatabase

/**
 * База для Robolectric-тестов DAO: in-memory Room с реальным SQLite,
 * чтобы проверять настоящие SQL-запросы (включая строковые сравнения дат).
 */
abstract class RoomDaoTest {

    protected lateinit var db: FinanceManagerDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, FinanceManagerDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        db.close()
    }
}
