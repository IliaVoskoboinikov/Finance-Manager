package soft.divan.financemanager.presenter.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import soft.divan.financemanager.core.feature_api.FeatureApi
import soft.divan.financemanager.core.string.R
import soft.divan.financemanager.uikit.icons.Calculator
import soft.divan.financemanager.uikit.icons.Chart90
import soft.divan.financemanager.uikit.icons.Downtrend
import soft.divan.financemanager.uikit.icons.Settings
import soft.divan.financemanager.uikit.icons.Uptrend


data class ScreenBottom(
    val feature: FeatureApi,
    val title: Int,
    val icon: ImageVector
) {
    companion object {
        fun items(
            expenses: FeatureApi,
            income: FeatureApi,
            account: FeatureApi,
            category: FeatureApi,
            settings: FeatureApi,
        ) = listOf(
            ScreenBottom(expenses, R.string.expenses, Icons.Filled.Downtrend),
            ScreenBottom(income, R.string.income, Icons.Filled.Uptrend),
            ScreenBottom(account, R.string.account, Icons.Filled.Calculator),
            ScreenBottom(category, R.string.category, Icons.Filled.Chart90),
            ScreenBottom(settings, R.string.settings, Icons.Filled.Settings),
        )
    }
}
