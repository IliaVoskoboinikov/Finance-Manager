package soft.divan.financemanager.presenter.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import soft.divan.financemanager.string.R
import soft.divan.financemanager.uikit.icons.Calculator
import soft.divan.financemanager.uikit.icons.Chart90
import soft.divan.financemanager.uikit.icons.Downtrend
import soft.divan.financemanager.uikit.icons.Settings
import soft.divan.financemanager.uikit.icons.Uptrend


sealed class ScreenBottom(
    val route: String,
    val title: Int,
    val icon: ImageVector,
) {
    data object ExpansesScreenBottom : ScreenBottom(
        "expanses",
        R.string.expanses,
        Icons.Filled.Downtrend,
    )

    data object IncomeScreenBottom : ScreenBottom(
        "income",
        R.string.income,
        Icons.Filled.Uptrend,
    )

    data object AccountScreenBottom : ScreenBottom(
        "account",
        R.string.account,
        Icons.Filled.Calculator,
    )

    data object ArticlesScreenBottom : ScreenBottom(
        "articleUis",
        R.string.articles,
        Icons.Filled.Chart90,
    )

    data object SettingsScreenBottom : ScreenBottom(
        "settings",
        R.string.settings,
        Icons.Filled.Settings,
    )

    companion object {
        val items = listOf(
            ExpansesScreenBottom,
            IncomeScreenBottom,
            AccountScreenBottom,
            ArticlesScreenBottom,
            SettingsScreenBottom
        )

        fun fromRoute(route: String?): ScreenBottom {
            return items.find { it.route == route } ?: ExpansesScreenBottom
        }
    }
}