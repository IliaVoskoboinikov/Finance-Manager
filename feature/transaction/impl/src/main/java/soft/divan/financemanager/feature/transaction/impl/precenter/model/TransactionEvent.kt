package soft.divan.financemanager.feature.transaction.impl.precenter.model

import androidx.annotation.StringRes

sealed interface TransactionEvent {
    data object TransactionDeleted : TransactionEvent
    data object TransactionSaved : TransactionEvent
    data class ShowError(@field:StringRes val messageRes: Int) : TransactionEvent
}