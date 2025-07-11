package soft.divan.financemanager.core.data

data class AccountStateEntity(
    val id: Int,
    val name: String,
    val balance: String,
    val currency: String,
)