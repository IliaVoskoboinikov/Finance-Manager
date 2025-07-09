package soft.divan.financemanager.core.network.dto

import com.google.gson.annotations.SerializedName

data class UpdateAccountRequestDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("balance")
    val balance: String,
    @SerializedName("currency")
    val currency: String
)