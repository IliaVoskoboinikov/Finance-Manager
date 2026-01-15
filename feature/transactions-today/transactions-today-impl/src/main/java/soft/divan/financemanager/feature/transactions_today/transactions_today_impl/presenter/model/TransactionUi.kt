package soft.divan.financemanager.feature.transactions_today.transactions_today_impl.presenter.model

data class TransactionUi(
    val id: String,
    val category: CategoryUi,
    val amountFormatted: String,
    val transactionDateTime: String,
    val comment: String,
)