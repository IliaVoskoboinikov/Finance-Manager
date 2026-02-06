package soft.divan.financemanager.core.data.dto

import com.google.gson.annotations.SerializedName

data class TransactionRequestDto(
    @SerializedName("accountId")
    val accountId: Int,

    @SerializedName("categoryId")
    val categoryId: Int,

    @SerializedName("amount")
    val amount: String,

    @SerializedName("transactionDate")
    val transactionDate: String,

    @SerializedName("comment")
    val comment: String? = null
)
// Revue me>>
