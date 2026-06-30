package soft.divan.financemanager.core.data.dto

import com.google.gson.annotations.SerializedName

data class CurrencyDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("name")
    val name: String
)
