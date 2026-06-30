package soft.divan.financemanager.feature.auth.impl.presenter.model

import androidx.compose.runtime.Immutable

@Immutable
data class AuthActions(
    val onUpdateName: (String) -> Unit = {},
    val onUpdatePassword: (String) -> Unit = {},
    val onToggleMode: () -> Unit = {},
    val onAuthClick: () -> Unit = {},
    val onMergeConfirm: (Boolean) -> Unit = {},
    val onLogoutClick: () -> Unit = {},
    val onLogoutConfirm: (Boolean) -> Unit = {},
    val onGuestClick: () -> Unit = {},
    val onDismissDialogs: () -> Unit = {}
)
