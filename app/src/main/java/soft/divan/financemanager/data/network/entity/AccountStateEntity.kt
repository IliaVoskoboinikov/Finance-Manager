package soft.divan.financemanager.data.network.entity

import com.google.gson.annotations.SerializedName

data class AccountStateEntity (
    val id: Int,
    val name: String,
    val balance: String,
    val currency: String,
)