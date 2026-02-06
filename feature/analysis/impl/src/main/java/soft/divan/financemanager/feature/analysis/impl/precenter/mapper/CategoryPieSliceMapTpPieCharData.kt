@file:Suppress("MagicNumber")

package soft.divan.financemanager.feature.analysis.impl.precenter.mapper

import androidx.compose.ui.graphics.Color
import co.yml.charts.common.model.PlotType
import co.yml.charts.ui.piechart.models.PieChartData
import soft.divan.financemanager.feature.analysis.impl.domain.model.CategoryPieSlice
import soft.divan.financemanager.uikit.theme.Blue
import soft.divan.financemanager.uikit.theme.CoralRed
import soft.divan.financemanager.uikit.theme.NeonMint
import soft.divan.financemanager.uikit.theme.Orange
import soft.divan.financemanager.uikit.theme.Pink
import soft.divan.financemanager.uikit.theme.Purple
import soft.divan.financemanager.uikit.theme.Purple80

fun List<CategoryPieSlice>.toPieChartData(): PieChartData {
    val slices = this.map { slice ->
        PieChartData.Slice(
            label = "${slice.emoji} ${slice.categoryName}",
            value = slice.percent,
            color = getRandomColorForCategory(slice.categoryId)
        )
    }

    return PieChartData(
        slices = slices,
        plotType = PlotType.Bar
    )
}

private fun getRandomColorForCategory(seed: Int): Color {
    val colors = listOf(
        Purple, // #9C27B0 — фиолетовый
        Purple80, // #F50000 — красный акцент
        NeonMint, // #2AE881 — яркая мята
        Orange, // #FF9800 — оранжевый
        Blue, // #2196F3 — синий
        Pink, // #E91E63 — ярко-розовый
        CoralRed, // #E46962 — коралловый

        Color(0xFFFFC107), // янтарный
        Color(0xFF4CAF50), // чистый зелёный
        Color(0xFF00BCD4), // бирюза
        Color(0xFF3F51B5), // индиго
        Color(0xFF8BC34A), // салатовый яркий
        Color(0xFFFF5722), // терракотовый огненный
        Color(0xFF009688), // глубокая тёмная бирюза
        Color(0xFF673AB7), // глубокий фиолетовый (иная насыщенность)
        Color(0xFFCDDC39), // лаймово-жёлтый
        Color(0xFF2196F3), // чистый яркий синий (дублировать не буду — уже есть)
        Color(0xFFE040FB), // ультра-фиолетовый
        Color(0xFF2962FF), // ярко-синий айс-неон
        Color(0xFFFF1744), // красный неон
        Color(0xFF00E5FF) // ледяная бирюза
    )
    return colors[seed % colors.size]
}
// Revue me>>
