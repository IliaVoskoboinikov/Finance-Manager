package soft.divan.financemanager.core.auth.data.dto

import com.google.gson.annotations.SerializedName

data class RefreshRequestDto(
    @SerializedName("refreshToken")
    val refreshToken: String?
)
