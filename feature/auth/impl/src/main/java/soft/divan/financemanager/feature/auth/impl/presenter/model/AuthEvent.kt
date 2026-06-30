package soft.divan.financemanager.feature.auth.impl.presenter.model

sealed interface AuthEvent {
    data object NavigateToMain : AuthEvent
    data class ShowToast(val message: String) : AuthEvent
    data class ShowError(val message: String) : AuthEvent
}
