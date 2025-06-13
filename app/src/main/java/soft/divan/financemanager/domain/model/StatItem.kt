package soft.divan.financemanager.domain.model

import android.icu.math.BigDecimal


data class StatItem(
    val categoryId: Int,
    val categoryName: String,
    val emoji: String,
    val amount: BigDecimal
)
