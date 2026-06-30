package soft.divan.financemanager.feature.auth.impl.presenter.model

sealed interface AuthAction {
    data class UpdateName(val name: String) : AuthAction
    data class UpdatePassword(val pass: String) : AuthAction
    data object ToggleMode : AuthAction
    data object OnAuthClick : AuthAction
    data class OnMergeConfirm(val shouldMerge: Boolean) : AuthAction
    data object OnLogoutClick : AuthAction
    data class OnLogoutConfirm(val shouldClear: Boolean) : AuthAction
    data object OnGuestClick : AuthAction
    data object DismissDialogs : AuthAction
}
