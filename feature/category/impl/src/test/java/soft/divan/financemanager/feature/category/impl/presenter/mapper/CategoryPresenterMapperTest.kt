package soft.divan.financemanager.feature.category.impl.presenter.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.feature.category.impl.presenter.model.UiCategory
import java.time.Instant

class CategoryPresenterMapperTest {

    private val category = Category(
        id = "cat-1",
        createdAt = Instant.parse("2024-01-01T00:00:00Z"),
        updatedAt = Instant.parse("2024-02-01T00:00:00Z"),
        name = "Food",
        emoji = "🍔",
        isIncome = false
    )

    @Test
    fun `toUi maps presentation fields`() {
        val ui = category.toUi()

        assertThat(ui).isEqualTo(
            UiCategory(id = "cat-1", name = "Food", emoji = "🍔", isIncome = false)
        )
    }

    @Test
    fun `toDomain maps fields back and stamps current time`() {
        val before = Instant.now()

        val domain = category.toUi().toDomain()

        assertThat(domain.id).isEqualTo("cat-1")
        assertThat(domain.name).isEqualTo("Food")
        assertThat(domain.emoji).isEqualTo("🍔")
        assertThat(domain.isIncome).isFalse()
        assertThat(domain.createdAt).isAfterOrEqualTo(before)
        assertThat(domain.updatedAt).isAfterOrEqualTo(before)
    }

    @Test
    fun `toUi preserves income flag`() {
        assertThat(category.copy(isIncome = true).toUi().isIncome).isTrue()
    }
}
