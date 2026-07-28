package soft.divan.financemanager.core.database.dao

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import soft.divan.financemanager.core.database.entity.CategoryEntity

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CategoryDaoTest : RoomDaoTest() {

    private val dao get() = db.categoryDao()

    private fun entity(id: String, name: String, isIncome: Boolean = false) = CategoryEntity(
        id = id,
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-01T00:00:00Z",
        name = name,
        emoji = "🍔",
        isIncome = isIncome
    )

    @Test
    fun `insertAll stores and replaces categories by id`() = runTest {
        dao.insertAll(listOf(entity("1", "Food"), entity("2", "Taxi")))
        dao.insertAll(listOf(entity("1", "Groceries")))

        val all = dao.getAll().first()

        assertThat(all).hasSize(2)
        assertThat(all.first { it.id == "1" }.name).isEqualTo("Groceries")
    }

    @Test
    fun `getByType filters by income flag and sorts by name`() = runTest {
        dao.insertAll(
            listOf(
                entity("1", "Zoo", isIncome = false),
                entity("2", "Auto", isIncome = false),
                entity("3", "Salary", isIncome = true)
            )
        )

        val expenses = dao.getByType(false).first()
        val incomes = dao.getByType(true).first()

        assertThat(expenses.map { it.name }).containsExactly("Auto", "Zoo")
        assertThat(incomes.map { it.name }).containsExactly("Salary")
    }

    @Test
    fun `getById returns category or null`() = runTest {
        dao.insertAll(listOf(entity("1", "Food")))

        assertThat(dao.getById("1")!!.name).isEqualTo("Food")
        assertThat(dao.getById("missing")).isNull()
    }
}
