package soft.divan.financemanager.feature.account.impl.precenter.model

import androidx.annotation.StringRes

sealed interface AccountEvent {
    data object Saved : AccountEvent
    data object Deleted : AccountEvent
    data class ShowError(@field:StringRes val messageRes: Int) : AccountEvent
}
// Revue me>>
