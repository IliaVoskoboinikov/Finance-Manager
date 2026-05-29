package soft.divan.financemanager.core.domain.model

data class Category(
    val id: String,
    val name: String,
    val emoji: String,
    val isIncome: Boolean
)
