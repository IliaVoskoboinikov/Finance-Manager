package soft.divan.financemanager.core.data.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class TransactionRequestDto(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("accountId")
    val accountId: String,
    @SerializedName("categoryId")
    val categoryId: String,
    @SerializedName("amount")
    val amount: BigDecimal,
    @SerializedName("dateTime")
    val dateTime: String,
    @SerializedName("comment")
    val comment: String? = null
)
