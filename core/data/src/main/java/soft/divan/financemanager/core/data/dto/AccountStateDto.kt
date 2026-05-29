package soft.divan.financemanager.core.data.dto

import com.google.gson.annotations.SerializedName

data class AccountStateDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("balance")
    val balance: String,
    @SerializedName("currencyId")
    val currencyId: String
)
