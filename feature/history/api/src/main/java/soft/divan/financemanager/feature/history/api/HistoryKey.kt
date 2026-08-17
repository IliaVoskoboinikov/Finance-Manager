package soft.divan.financemanager.feature.history.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Экран истории операций за период.
 *
 * @param isIncome `true` — история доходов, `false` — история расходов.
 */
@Serializable
data class HistoryKey(val isIncome: Boolean) : NavKey
