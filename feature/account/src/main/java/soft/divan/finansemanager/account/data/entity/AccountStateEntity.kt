package soft.divan.finansemanager.account.data.entity

data class AccountStateEntity(
    val id: Int,
    val name: String,
    val balance: String,
    val currency: String,
)