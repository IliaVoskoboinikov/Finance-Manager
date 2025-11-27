package soft.divan.financemanager.feature.account.my_accounts_impl.presenter.model

data class AccountUiModel(
    val id: Int,
    val name: String,
    val balance: String,
    val currency: String
)