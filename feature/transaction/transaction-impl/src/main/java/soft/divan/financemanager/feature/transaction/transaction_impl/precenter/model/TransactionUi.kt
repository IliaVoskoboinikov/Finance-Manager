package soft.divan.financemanager.feature.transaction.transaction_impl.precenter.model

data class TransactionUi(
    val id: String,
    val accountId: String,
    val category: CategoryUi,
    val currencyCode: String,
    val amount: String,
    val date: String,
    val time: String,
    val comment: String,
    val createdAt: String,
    val updatedAt: String,
    val mode: TransactionMode
)