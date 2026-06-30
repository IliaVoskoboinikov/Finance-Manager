package soft.divan.financemanager.feature.auth.impl.presenter.model

import androidx.compose.runtime.Immutable

@Immutable
data class AuthUi(
    val name: String = "",
    val pass: String = "",
    val isLoginMode: Boolean = true,
    val pendingCredentials: Pair<String, String>? = null
)
