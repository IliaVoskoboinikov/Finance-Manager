package soft.divan.financemanager.core.data.dto

import com.google.gson.annotations.SerializedName

data class UpdateAccountRequestDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("balance")
    val balance: Double,
    @SerializedName("currencyId")
    val currencyId: String
)
