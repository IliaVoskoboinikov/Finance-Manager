package soft.divan.financemanager.feature.analysis.impl.precenter.mapper

import co.yml.charts.common.model.PlotType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.feature.analysis.impl.domain.model.CategoryPieSlice
import java.math.BigDecimal

class CategoryPieSliceMapTpPieCharDataTest {

    private fun slice(
        categoryId: String = "cat-1",
        name: String = "Food",
        emoji: String = "🍔",
        percentage: Float = 42.5f
    ) = CategoryPieSlice(
        categoryId = categoryId,
        categoryName = name,
        emoji = emoji,
        amount = BigDecimal.TEN,
        percentage = percentage
    )

    @Test
    fun `maps slices with emoji-name label and percentage value`() {
        val data = listOf(slice()).toPieChartData()

        assertThat(data.slices).hasSize(1)
        assertThat(data.slices.first().label).isEqualTo("🍔 Food")
        assertThat(data.slices.first().value).isEqualTo(42.5f)
    }

    @Test
    fun `uses Bar plot type`() {
        assertThat(listOf(slice()).toPieChartData().plotType).isEqualTo(PlotType.Bar)
    }

    @Test
    fun `empty slice list maps to empty chart`() {
        assertThat(emptyList<CategoryPieSlice>().toPieChartData().slices).isEmpty()
    }

    @Test
    fun `color is deterministic for the same category id`() {
        val first = listOf(slice(categoryId = "stable-id")).toPieChartData().slices.first().color
        val second = listOf(slice(categoryId = "stable-id")).toPieChartData().slices.first().color

        assertThat(first).isEqualTo(second)
    }

    @Test
    fun `maps every slice in order`() {
        val data = listOf(
            slice(categoryId = "1", name = "Food", percentage = 60f),
            slice(categoryId = "2", name = "Taxi", emoji = "🚕", percentage = 40f)
        ).toPieChartData()

        assertThat(data.slices.map { it.label }).containsExactly("🍔 Food", "🚕 Taxi")
        assertThat(data.slices.map { it.value }).containsExactly(60f, 40f)
    }
}
