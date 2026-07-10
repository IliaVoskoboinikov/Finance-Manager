package soft.divan.financemanager.core.data.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class UpdateAccountRequestDto(
    @SerializedName("name")
    val name: String,
    @SerializedName("balance")
    val balance: BigDecimal,
    @SerializedName("currencyId")
    val currencyId: String
)
