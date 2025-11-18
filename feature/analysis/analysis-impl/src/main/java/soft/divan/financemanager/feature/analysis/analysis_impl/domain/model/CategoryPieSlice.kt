package soft.divan.financemanager.feature.analysis.analysis_impl.domain.model

import java.math.BigDecimal

data class CategoryPieSlice(
    val categoryId: Int,
    val categoryName: String,
    val emoji: String,
    val totalAmount: BigDecimal,
    val percent: Float
)