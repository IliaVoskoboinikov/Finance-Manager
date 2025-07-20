package soft.divan.financemanager.core.data.entity

import soft.divan.financemanager.core.data.mapper.AccountStateEntity
import soft.divan.finansemanager.core.database.entity.CategoryEntity

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