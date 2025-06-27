package soft.divan.financemanager.presenter.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration.Indefinite
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import soft.divan.financemanager.R
import soft.divan.financemanager.presenter.MainViewModel
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

@SuppressLint("FlowOperatorInvokedInComposition")
@Composable
fun MainScreen(modifier: Modifier = Modifier, viewModel: MainViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val currentDestination by navController.currentBackStackEntryAsState()
    val currentScreenBottom = ScreenBottom.fromRoute(currentDestination?.destination?.route)
    val showBars = currentDestination?.destination?.route != SplashScreen.route
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    val bottomRoutes = ScreenBottom.items.map { it.route }
    val updateBalanceAccountViewModel: UpdateBalanceAccountViewModel = hiltViewModel()
    val accountViewModel: AccountViewModel = hiltViewModel()
    val state by accountViewModel.uiState.collectAsStateWithLifecycle()

    val title = when (currentDestination?.destination?.route) {
        ScreenBottom.ExpansesScreenBottom.route -> TopBarModel.ExpansesTopBar
        ScreenBottom.IncomeScreenBottom.route -> TopBarModel.IncomeTopBar
        ScreenBottom.AccountScreenBottom.route -> TopBarModel.AccountTopBar
        ScreenBottom.ArticlesScreenBottom.route -> TopBarModel.ArticlesTopBar
        ScreenBottom.SettingsScreenBottom.route -> TopBarModel.SettingsTopBar
        HistoryExpensesScreen.route -> TopBarModel.HistoryTopBar
        HistoryIncomeScreen.route -> TopBarModel.HistoryTopBar
        AddAccountScreen.route -> TopBarModel.AddAccountTopBar {
            if (state is AccountUiState.Success) {
                // будет реализация по заданию в 4 дз
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

    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier,
        snackbarHost = {
            SnackbarHost(
                snackbarHostState,
                modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
            )
        },
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
            val notConnectedMessage = stringResource(R.string.not_connected)
            LaunchedEffect(isOffline) {
                if (isOffline) {
                    snackbarHostState.showSnackbar(
                        message = notConnectedMessage,
                        duration = Indefinite,
                    )
                }
            }
        }


    }

}




