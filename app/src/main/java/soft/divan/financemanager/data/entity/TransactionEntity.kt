package soft.divan.financemanager.data.entity

import soft.divan.financemanager.category.data.entity.CategoryEntity
import soft.divan.finansemanager.account.data.entity.AccountStateEntity

data class TransactionEntity(
    val id: Int,
    val account: AccountStateEntity,
    val category: CategoryEntity,
    val amount: String,
    val transactionDate: String,
    val comment: String,
    val createdAt: String,
    val updatedAt: String,
)