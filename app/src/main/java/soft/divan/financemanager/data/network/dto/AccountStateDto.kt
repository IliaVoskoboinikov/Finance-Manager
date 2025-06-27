package soft.divan.financemanager.data.network.dto

import com.google.gson.annotations.SerializedName

data class AccountStateDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("balance")
    val balance: String,
    @SerializedName("currency")
    val currency: String,
)