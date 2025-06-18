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
        Screen.Expanses.route -> TopBar.Expanses
        Screen.Income.route -> TopBar.Income
        Screen.Account.route -> TopBar.Account
        Screen.Articles.route -> TopBar.Articles
        Screen.Settings.route -> TopBar.Settings

        else -> {
            TopBar.Expanses
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
    data object Expanses : Screen(
        "expanses",
        R.string.expanses,
        Icons.Filled.Downtrend,
    )

    data object Income : Screen(
        "income",
        R.string.income,
        Icons.Filled.Uptrend,
    )

    data object Account : Screen(
        "account",
        R.string.account,
        Icons.Filled.Calculator,
    )

    data object Articles : Screen(
        "articles",
        R.string.articles,
        Icons.Filled.Chart90,
    )

    data object Settings : Screen(
        "settings",
        R.string.settings,
        Icons.Filled.Settings,
    )

    companion object {
        val items = listOf(Expanses, Income, Account, Articles, Settings)
        fun fromRoute(route: String?): Screen {
            return items.find { it.route == route } ?: Expanses
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
    data object Expanses : TopBar(R.string.expanses_today, null, {}, Icons.Filled.Clock, {})
    data object Income : TopBar(R.string.income_today, null, {}, Icons.Filled.Clock, {})
    data object Account : TopBar(R.string.my_account, null, {}, Icons.Filled.Pencil, {})
    data object Articles : TopBar(R.string.my_articles, null, {}, null, {})
    data object Settings : TopBar(R.string.settings, null, {}, null, {})
}


