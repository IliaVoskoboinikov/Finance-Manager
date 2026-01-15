package soft.divan.financemanager.feature.transaction.transaction_impl.precenter.model

sealed interface TransactionMode {
    data object Create : TransactionMode
    data class Edit(val id: String) : TransactionMode
}