package soft.divan.financemanager.core.data.dto

import com.google.gson.annotations.SerializedName

data class UpdateTransactionRequestDto(
    @SerializedName("accountId")
    val accountId: String,
    @SerializedName("categoryId")
    val categoryId: String,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("dateTime")
    val dateTime: String,
    @SerializedName("comment")
    val comment: String? = null
)
