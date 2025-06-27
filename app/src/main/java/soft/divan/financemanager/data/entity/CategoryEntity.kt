package soft.divan.financemanager.data.entity

data class CategoryEntity(
    val id: Int,
    val name: String,
    val emoji: String,
    val isIncome: Boolean,
)