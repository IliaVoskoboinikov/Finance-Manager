package soft.divan.financemanager.presenter.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import soft.divan.financemanager.R
import soft.divan.financemanager.feature.category.api.CategoryKey
import soft.divan.financemanager.feature.myaccounts.impl.MyAccountsKey
import soft.divan.financemanager.feature.settings.api.SettingsKey
import soft.divan.financemanager.feature.transactionstoday.api.TransactionsTodayKey
import soft.divan.financemanager.uikit.icons.Calculator
import soft.divan.financemanager.uikit.icons.Chart90
import soft.divan.financemanager.uikit.icons.Downtrend
import soft.divan.financemanager.uikit.icons.Settings
import soft.divan.financemanager.uikit.icons.Uptrend

/**
 * Вкладка нижней навигации: корневой ключ её back stack, подпись и иконка.
 */
data class ScreenBottom(
    val key: NavKey,
    val title: Int,
    val icon: ImageVector
) {
    companion object {
        /**
         * Вкладки в порядке отображения. Первая вкладка — стартовая: на неё возвращает
         * системный «назад» с корневого экрана любой другой вкладки.
         */
        fun items() = listOf(
            ScreenBottom(
                key = TransactionsTodayKey(isIncome = false),
                title = R.string.expenses,
                icon = Icons.Filled.Downtrend
            ),
            ScreenBottom(
                key = TransactionsTodayKey(isIncome = true),
                title = R.string.income,
                icon = Icons.Filled.Uptrend
            ),
            ScreenBottom(
                key = MyAccountsKey,
                title = R.string.account,
                icon = Icons.Filled.Calculator
            ),
            ScreenBottom(
                key = CategoryKey,
                title = R.string.category,
                icon = Icons.Filled.Chart90
            ),
            ScreenBottom(
                key = SettingsKey,
                title = R.string.settings,
                icon = Icons.Filled.Settings
            )
        )
    }
}
