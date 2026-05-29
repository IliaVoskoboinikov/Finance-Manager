package soft.divan.financemanager.core.domain.model

import java.time.Instant

data class Category(
    val id: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val name: String,
    val emoji: String,
    val isIncome: Boolean
)
