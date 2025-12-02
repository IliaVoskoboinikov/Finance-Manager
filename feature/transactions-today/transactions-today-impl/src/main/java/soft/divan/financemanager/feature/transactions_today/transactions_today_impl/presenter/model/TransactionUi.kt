package soft.divan.financemanager.feature.transactions_today.transactions_today_impl.presenter.model

data class TransactionUi(
    val id: Int,
    val category: CategoryUi,
    val amountFormatted: String,
    val transactionDateTime: String,
    val comment: String,
)