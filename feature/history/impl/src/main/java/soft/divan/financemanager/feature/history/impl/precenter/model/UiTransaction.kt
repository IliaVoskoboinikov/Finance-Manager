package soft.divan.financemanager.feature.history.impl.precenter.model

data class UiTransaction(
    val id: String,
    val category: UiCategory,
    val amountFormatted: String,
    val transactionDateTime: String,
    val comment: String,
)
