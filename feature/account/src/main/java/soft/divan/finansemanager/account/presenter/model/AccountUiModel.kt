package soft.divan.finansemanager.account.presenter.model

data class AccountUiModel(
    val id: Int,
    val name: String,
    val balance: String,
    val currency: String
)