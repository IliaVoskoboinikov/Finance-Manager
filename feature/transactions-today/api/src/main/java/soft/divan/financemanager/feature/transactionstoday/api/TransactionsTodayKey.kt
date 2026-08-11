package soft.divan.financemanager.feature.transactionstoday.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Экран операций за сегодня.
 *
 * Расходы и доходы — это два разных ключа одного экрана: они образуют две независимые
 * вкладки нижней навигации со своими back stack.
 *
 * @param isIncome `true` — доходы, `false` — расходы.
 */
@Serializable
data class TransactionsTodayKey(val isIncome: Boolean) : NavKey
