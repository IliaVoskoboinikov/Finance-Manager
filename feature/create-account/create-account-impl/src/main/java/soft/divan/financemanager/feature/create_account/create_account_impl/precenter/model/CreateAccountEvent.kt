package soft.divan.financemanager.feature.create_account.create_account_impl.precenter.model

import androidx.annotation.StringRes

sealed interface CreateAccountEvent {
    data object Saved : CreateAccountEvent
    data class ShowError(@field:StringRes val messageRes: Int) : CreateAccountEvent
}