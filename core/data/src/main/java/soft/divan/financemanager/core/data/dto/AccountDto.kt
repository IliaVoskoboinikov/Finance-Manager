package soft.divan.financemanager.core.data.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class AccountDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("userId")
    val userId: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("balance")
    val balance: BigDecimal,
    @SerializedName("currencyId")
    val currencyId: String,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
)
