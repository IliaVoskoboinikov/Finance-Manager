package soft.divan.financemanager.data.network.entity

import com.google.gson.annotations.SerializedName

data class CategoryEntity (
    val id: Int,
    val name: String ,
    val emoji: String ,
    val isIncome: Boolean,
)