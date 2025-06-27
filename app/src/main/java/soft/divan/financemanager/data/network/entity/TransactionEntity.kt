package soft.divan.financemanager.data.network.entity

import com.google.gson.annotations.SerializedName
import soft.divan.financemanager.data.network.dto.AccountStateDto
import soft.divan.financemanager.data.network.dto.CategoryDto
import soft.divan.financemanager.domain.model.AccountState

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