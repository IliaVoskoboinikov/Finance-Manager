package soft.divan.financemanager.data.entity

data class TransactionEntity (
    val id: Int,
    val account: AccountStateEntity,
    val category: CategoryEntity,
    val amount: String,
    val transactionDate: String,
    val comment: String,
    val createdAt: String,
    val updatedAt: String,
)