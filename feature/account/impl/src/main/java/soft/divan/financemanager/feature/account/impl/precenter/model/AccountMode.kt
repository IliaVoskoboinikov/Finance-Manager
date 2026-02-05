package soft.divan.financemanager.feature.account.impl.precenter.model

sealed interface AccountMode {
    data object Create : AccountMode
    data class Edit(val id: String) : AccountMode
}
