package soft.divan.financemanager.feature.analysis.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Экран аналитики по операциям за период.
 *
 * @param isIncome `true` — аналитика доходов, `false` — аналитика расходов.
 */
@Serializable
data class AnalysisKey(val isIncome: Boolean) : NavKey
