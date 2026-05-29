package soft.divan.financemanager.core.data.dto

import com.google.gson.annotations.SerializedName

data class TransactionResponseCreateDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("accountId")
    val accountId: String,
    @SerializedName("categoryId")
    val categoryId: String,
    @SerializedName("amount")
    val amount: String,
    @SerializedName("transactionDate")
    val transactionDate: String,
    @SerializedName("comment")
    val comment: String? = null,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
)
