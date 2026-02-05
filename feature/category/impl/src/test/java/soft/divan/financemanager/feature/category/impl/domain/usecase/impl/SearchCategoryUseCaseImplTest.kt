package soft.divan.financemanager.feature.category.impl.domain.usecase.impl

import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import soft.divan.financemanager.core.domain.model.Category

class SearchCategoryUseCaseImplTest {

    private val useCase = SearchCategoryUseCaseImpl()

    @Test
    fun `invoke should return categories matching query ignoring case`() = runTest {
        // Arrange
        val query = "foo"
        val categories = listOf(
            Category(id = 1, name = "Food", emoji = "🍔", isIncome = false),
            Category(id = 2, name = "Football", emoji = "⚽", isIncome = false),
            Category(id = 3, name = "Salary", emoji = "💰", isIncome = true),
            Category(id = 4, name = "Transport", emoji = "🚗", isIncome = false)
        )

        // Act
        val result = useCase.invoke(query, categories)

        // Assert
        Assert.assertEquals(2, result.size)
        Assert.assertEquals(listOf("Food", "Football"), result.map { it.name })
    }

    @Test
    fun `invoke returns empty list when no categories match query`() = runTest {
        // Arrange
        val query = "Taxi"
        val categories = listOf(
            Category(1, "Food", "🍔", false),
            Category(2, "Salary", "💰", true)
        )

        // Act
        val result = useCase.invoke(query, categories)

        // Assert
        Assert.assertEquals(emptyList<Category>(), result)
    }

    @Test
    fun `invoke returns all categories when query is empty`() = runTest {
        // Arrange
        val query = ""
        val categories = listOf(
            Category(1, "Food", "🍔", false),
            Category(2, "Salary", "💰", true)
        )

        // Act
        val result = useCase.invoke(query, categories)

        // Assert
        Assert.assertEquals(categories, result)
    }

    @Test
    fun `invoke returns empty list when categories list is empty`() = runTest {
        // Arrange
        val query = "Food"
        val categories = emptyList<Category>()

        // Act
        val result = useCase.invoke(query, categories)

        // Assert
        Assert.assertEquals(emptyList<Category>(), result)
    }

    @Test
    fun `invoke returns category when query fully matches name`() = runTest {
        // Arrange
        val query = "Salary"
        val categories = listOf(
            Category(1, "Food", "🍔", false),
            Category(2, "Salary", "💰", true)
        )

        // Act
        val result = useCase.invoke(query, categories)

        // Assert
        Assert.assertEquals(1, result.size)
        Assert.assertEquals("Salary", result.first().name)
    }

    @Test
    fun `invoke returns empty list when query longer than any category name`() = runTest {
        // Arrange
        val query = "VeryLongQueryThatDoesNotMatch"
        val categories = listOf(
            Category(1, "Food", "🍔", false),
            Category(2, "Rent", "🏠", false)
        )

        // Act
        val result = useCase.invoke(query, categories)

        // Assert
        Assert.assertEquals(emptyList<Category>(), result)
    }
}
