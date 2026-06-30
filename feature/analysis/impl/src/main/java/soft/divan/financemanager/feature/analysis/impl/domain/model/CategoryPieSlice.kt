package soft.divan.financemanager.feature.analysis.impl.domain.model

import java.math.BigDecimal

data class CategoryPieSlice(
    val categoryId: String,
    val categoryName: String,
    val emoji: String,
    val amount: BigDecimal,
    val percentage: Float
)
