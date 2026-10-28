package soft.divan.financemanager.feature.auth.impl.presenter.model

import androidx.annotation.StringRes

sealed interface AuthEvent {
    data object NavigateToMain : AuthEvent

    /**
     * Короткое уведомление пользователю. Несёт ресурс, а не готовый текст: строку разрешает
     * экран, поэтому она берётся в текущей локали приложения.
     */
    data class ShowToast(@field:StringRes val message: Int) : AuthEvent
    data class ShowError(val message: String) : AuthEvent
}
