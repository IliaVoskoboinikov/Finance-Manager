package soft.divan.financemanager.core.data.dto

import com.google.gson.annotations.SerializedName

data class TransactionRequestDto(
    @SerializedName("accountId")
    val accountId: String,

    @SerializedName("categoryId")
    val categoryId: String,

    @SerializedName("amount")
    val amount: String,

    @SerializedName("transactionDate")
    val transactionDate: String,

    @SerializedName("comment")
    val comment: String? = null
)
