package soft.divan.financemanager.presenter.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import soft.divan.financemanager.core.feature_api.FeatureApi
import soft.divan.financemanager.string.R
import soft.divan.financemanager.uikit.icons.Calculator
import soft.divan.financemanager.uikit.icons.Chart90
import soft.divan.financemanager.uikit.icons.Downtrend
import soft.divan.financemanager.uikit.icons.Settings
import soft.divan.financemanager.uikit.icons.Uptrend



sealed class ScreenBottom(
    val feature: FeatureApi,
    val title: Int,
    val icon: ImageVector
) {
    class DynamicScreenBottom(
        feature: FeatureApi,
        title: Int,
        icon: ImageVector
    ) : ScreenBottom(feature, title, icon)

    companion object {
        fun items(
            expenses: FeatureApi,
            income: FeatureApi,
            account: FeatureApi,
            category: FeatureApi,
            settings: FeatureApi,
        ) = listOf(
            DynamicScreenBottom(expenses, R.string.expenses, Icons.Filled.Downtrend),
            DynamicScreenBottom(income, R.string.income, Icons.Filled.Uptrend),
            DynamicScreenBottom(account, R.string.account, Icons.Filled.Calculator),
            DynamicScreenBottom(category, R.string.category, Icons.Filled.Chart90),
            DynamicScreenBottom(settings, R.string.settings, Icons.Filled.Settings),
        )
    }
}
