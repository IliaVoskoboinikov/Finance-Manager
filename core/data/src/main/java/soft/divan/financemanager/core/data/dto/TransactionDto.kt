package soft.divan.financemanager.core.data.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class TransactionDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("accountId")
    val accountId: String,
    @SerializedName("categoryId")
    val categoryId: String,
    @SerializedName("amount")
    val amount: BigDecimal,
    @SerializedName("dateTime")
    val dateTime: String,
    @SerializedName("comment")
    val comment: String?
)
