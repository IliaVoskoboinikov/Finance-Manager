package soft.divan.financemanager.core.data.dto

import com.google.gson.annotations.SerializedName

data class CreateAccountRequestDto(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("name")
    val name: String,
    @SerializedName("balance")
    val balance: Double,
    @SerializedName("currencyId")
    val currencyId: String
)
