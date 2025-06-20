package soft.divan.financemanager.presenter.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import soft.divan.financemanager.domain.model.AccountBrief
import soft.divan.financemanager.presenter.navigation.BottomNavigationBar
import soft.divan.financemanager.presenter.navigation.NavGraph
import soft.divan.financemanager.presenter.navigation.ScreenBottom
import soft.divan.financemanager.presenter.ui.model.AccountUiState
import soft.divan.financemanager.presenter.ui.model.TopBarModel
import soft.divan.financemanager.presenter.ui.theme.FinanceManagerTheme
import soft.divan.financemanager.presenter.ui.viewmodel.AccountViewModel
import soft.divan.financemanager.presenter.ui.viewmodel.UpdateBalanceAccountViewModel
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
    val currentScreenBottom = ScreenBottom.fromRoute(currentDestination?.destination?.route)
    val showBars = currentDestination?.destination?.route != SplashScreen.route
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    val bottomRoutes = ScreenBottom.items.map { it.route }
    val updateBalanceAccountViewModel: UpdateBalanceAccountViewModel = hiltViewModel()
    val accountViewModel: AccountViewModel = hiltViewModel()
    val state by accountViewModel.uiState.collectAsState()

    val title = when (currentDestination?.destination?.route) {
        ScreenBottom.ExpansesScreenBottom.route -> TopBarModel.ExpansesTopBar
        ScreenBottom.IncomeScreenBottom.route -> TopBarModel.IncomeTopBar
        ScreenBottom.AccountScreenBottom.route -> TopBarModel.AccountTopBar
        ScreenBottom.ArticlesScreenBottom.route -> TopBarModel.ArticlesTopBar
        ScreenBottom.SettingsScreenBottom.route -> TopBarModel.SettingsTopBar
        HistoryScreen.route -> TopBarModel.HistoryTopBar
        AddAccountScreen.route -> TopBarModel.AddAccountTopBar {


            if (state is AccountUiState.Success) {
                updateBalanceAccountViewModel.updateBalance(
                    AccountBrief(
                        id = (state as AccountUiState.Success).account.id,
                        name = (state as AccountUiState.Success).account.name,
                        balance = (state as AccountUiState.Success).account.balance,
                        currency = (state as AccountUiState.Success).account.currency
                    )
                )
                navController.popBackStack()
            }
        }
        else -> {
            TopBarModel.ExpansesTopBar
        }
    }
// если текущий маршрут НЕ входит в нижнюю навигацию, сохраняем последний выбранный из нижнего меню
    val effectiveRoute = if (currentRoute in bottomRoutes) {
        currentRoute
    } else {
        bottomRoutes.firstOrNull { it in navController.previousBackStackEntry?.destination?.route.orEmpty() }
            ?: bottomRoutes.first()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            if (showBars) {
                TopBar(topBar = title, navController = navController)
            }
        },
        bottomBar = {
            if (showBars) {
                BottomNavigationBar(
                    navController = navController,
                    currentRoute = effectiveRoute
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




