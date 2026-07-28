package soft.divan.financemanager.feature.category.impl.presenter.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CategoryMockDataTest {

    @Test
    fun `mock categories contain income and expense samples`() {
        assertThat(mockCategories).isNotEmpty()
        assertThat(mockCategories.map { it.id }).doesNotHaveDuplicates()
        assertThat(mockCategories.any { it.isIncome }).isTrue()
        assertThat(mockCategories.any { !it.isIncome }).isTrue()
    }

    @Test
    fun `mock success state exposes full list as filtered`() {
        assertThat(mockCategoriesUiStateSuccess.categories).isEqualTo(mockCategories)
        assertThat(mockCategoriesUiStateSuccess.filteredCategories).isEqualTo(mockCategories)
    }
}
