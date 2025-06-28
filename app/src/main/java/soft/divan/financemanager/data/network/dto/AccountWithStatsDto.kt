package soft.divan.financemanager.data.network.dto

import com.google.gson.annotations.SerializedName

data class AccountWithStatsDto(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("balance")
    val balance: String,
    @SerializedName("currency")
    val currency: String,
    @SerializedName("incomeStats")
    val incomeStats: List<CategoryStatDto>,
    @SerializedName("expenseStats")
    val expenseStats: List<CategoryStatDto>,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String
)