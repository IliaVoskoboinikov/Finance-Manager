package soft.divan.financemanager.feature.account.account_impl.precenter.model

sealed interface AccountMode {
    data object Create : AccountMode
    data class Edit(val id: Int) : AccountMode
}