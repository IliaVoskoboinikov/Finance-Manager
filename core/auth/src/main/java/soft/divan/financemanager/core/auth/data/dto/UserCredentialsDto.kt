package soft.divan.financemanager.core.auth.data.dto

import com.google.gson.annotations.SerializedName

data class UserCredentialsDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("password")
    val password: String
)
