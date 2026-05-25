package soft.divan.financemanager.feature.auth.impl.presenter.model

import androidx.compose.runtime.Immutable

@Immutable
data class ProfileActions(
    val onNavigateToAuth: () -> Unit = {},
    val onLogoutClick: () -> Unit = {},
    val onLogoutConfirm: (Boolean) -> Unit = {},
    val onDismissDialogs: () -> Unit = {}
)
