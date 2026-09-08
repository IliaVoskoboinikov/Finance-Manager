package soft.divan.financemanager.feature.account.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Экран счёта.
 *
 * @param accountId идентификатор редактируемого счёта; `null` — создание нового счёта.
 */
@Serializable
data class AccountKey(val accountId: String? = null) : NavKey
