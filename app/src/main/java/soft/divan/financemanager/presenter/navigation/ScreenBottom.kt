package soft.divan.financemanager.presenter.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import soft.divan.financemanager.R
import soft.divan.financemanager.core.feature_api.FeatureApi
import soft.divan.financemanager.feature.transactions_today.transactions_today_api.TransactionsTodayFeatureApi
import soft.divan.financemanager.uikit.icons.Calculator
import soft.divan.financemanager.uikit.icons.Chart90
import soft.divan.financemanager.uikit.icons.Downtrend
import soft.divan.financemanager.uikit.icons.Settings
import soft.divan.financemanager.uikit.icons.Uptrend


data class ScreenBottom(
    val route: String,
    val title: Int,
    val icon: ImageVector
) {
    companion object {
        fun items(
            transactionsToday: TransactionsTodayFeatureApi,

            myAccounts: FeatureApi,
            category: FeatureApi,
            settings: FeatureApi,
        ) = listOf(
            ScreenBottom(transactionsToday.route, R.string.expenses, Icons.Filled.Downtrend),
            ScreenBottom(transactionsToday.route2, R.string.income, Icons.Filled.Uptrend),
            ScreenBottom(myAccounts.route, R.string.account, Icons.Filled.Calculator),
            ScreenBottom(category.route, R.string.category, Icons.Filled.Chart90),
            ScreenBottom(settings.route, R.string.settings, Icons.Filled.Settings),
        )
    }
}
