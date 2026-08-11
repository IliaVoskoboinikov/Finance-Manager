package soft.divan.financemanager.feature.transaction.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Экран операции.
 *
 * @param isIncome `true` — доход, `false` — расход.
 * @param transactionId идентификатор редактируемой операции; `null` — создание новой.
 */
@Serializable
data class TransactionKey(
    val isIncome: Boolean,
    val transactionId: String? = null
) : NavKey
