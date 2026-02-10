package soft.divan.financemanager.feature.transactionstoday.impl.presenter.model

data class TransactionUi(
    val id: String,
    val category: CategoryUi,
    val amountFormatted: String,
    val transactionDateTime: String,
    val comment: String
)
// Revue me>>
