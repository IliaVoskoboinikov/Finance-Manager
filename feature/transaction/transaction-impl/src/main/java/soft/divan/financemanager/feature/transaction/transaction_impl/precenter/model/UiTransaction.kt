package soft.divan.financemanager.feature.transaction.transaction_impl.precenter.model

data class UiTransaction(
    val id: Int?,
    val accountId: Int,
    val category: UiCategory,
    val currencyCode: String,
    val amount: String,
    val date: String,
    val time: String,
    val comment: String,
    val createdAt: String,
    val updatedAt: String,
    val mode: TransactionMode
)