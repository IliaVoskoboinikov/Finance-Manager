package soft.divan.financemanager.presenter.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import soft.divan.financemanager.R
import soft.divan.financemanager.presenter.navigation.BottomNavigationBar
import soft.divan.financemanager.presenter.navigation.NavGraph
import soft.divan.financemanager.presenter.ui.icons.Calculator
import soft.divan.financemanager.presenter.ui.icons.Chart90
import soft.divan.financemanager.presenter.ui.icons.Clock
import soft.divan.financemanager.presenter.ui.icons.Downtrend
import soft.divan.financemanager.presenter.ui.icons.Pencil
import soft.divan.financemanager.presenter.ui.icons.Settings
import soft.divan.financemanager.presenter.ui.icons.Uptrend
import soft.divan.financemanager.presenter.ui.theme.FinanceManagerTheme
import soft.divan.financemanager.presenter.uiKit.TopBar


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun MainScreenPreview() {
    FinanceManagerTheme {
        MainScreen()
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val currentDestination by navController.currentBackStackEntryAsState()
    val currentScreen = Screen.fromRoute(currentDestination?.destination?.route)
    val showBars = currentDestination?.destination?.route != SplashScreen.route

    val title = when (currentDestination?.destination?.route) {
        Screen.ExpansesScreen.route -> TopBar.ExpansesTopBar
        Screen.IncomeScreen.route -> TopBar.IncomeTopBar
        Screen.AccountScreen.route -> TopBar.AccountTopBar
        Screen.ArticlesScreen.route -> TopBar.ArticlesTopBar
        Screen.SettingsScreen.route -> TopBar.SettingsTopBar

        else -> {
            TopBar.ExpansesTopBar
        }
    }


    Scaffold(
        modifier = modifier,
        topBar = {
            if (showBars) {
                TopBar(topBar = title)
            }
        },
        bottomBar = {
            if (showBars) {
                BottomNavigationBar(
                    navController = navController,
                    currentRoute = currentDestination?.destination?.route
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)

        ) {
            NavGraph(navController = navController)
        }
    }

}


sealed class Screen(
    val route: String,
    val title: Int,
    val icon: ImageVector,
) {
    data object ExpansesScreen : Screen(
        "expanses",
        R.string.expanses,
        Icons.Filled.Downtrend,
    )

    data object IncomeScreen : Screen(
        "income",
        R.string.income,
        Icons.Filled.Uptrend,
    )

    data object AccountScreen : Screen(
        "account",
        R.string.account,
        Icons.Filled.Calculator,
    )

    data object ArticlesScreen : Screen(
        "articles",
        R.string.articles,
        Icons.Filled.Chart90,
    )

    data object SettingsScreen : Screen(
        "settings",
        R.string.settings,
        Icons.Filled.Settings,
    )

    companion object {
        val items = listOf(ExpansesScreen, IncomeScreen, AccountScreen, ArticlesScreen, SettingsScreen)
        fun fromRoute(route: String?): Screen {
            return items.find { it.route == route } ?: ExpansesScreen
        }
    }
}


sealed class TopBar(
    val title: Int,
    val navigationIcon: ImageVector? = null,
    val navigationIconClick: () -> Unit = {},
    val actionIcon: ImageVector? = null,
    val actionIconClick: () -> Unit = {},
) {
    data object ExpansesTopBar : TopBar(R.string.expanses_today, null, {}, Icons.Filled.Clock, {})
    data object IncomeTopBar : TopBar(R.string.income_today, null, {}, Icons.Filled.Clock, {})
    data object AccountTopBar : TopBar(R.string.my_account, null, {}, Icons.Filled.Pencil, {})
    data object ArticlesTopBar : TopBar(R.string.my_articles, null, {}, null, {})
    data object SettingsTopBar : TopBar(R.string.settings, null, {}, null, {})
}


