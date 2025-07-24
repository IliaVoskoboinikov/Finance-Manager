package soft.divan.financemanager.presenter.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarDuration.Indefinite
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import soft.divan.financemanager.feature.account.account_impl.AccountFeatureApi
import soft.divan.financemanager.feature.category.category_api.CategoryFeatureApi
import soft.divan.financemanager.feature.expenses.expenses_api.ExpensesFeatureApi
import soft.divan.financemanager.feature.income.income_api.IncomeFeatureApi
import soft.divan.financemanager.feature.settings.settings_api.SettingsFeatureApi
import soft.divan.financemanager.feature.splash_screen.splash_screen_api.SplashScreenFeatureApi
import soft.divan.financemanager.feature.transaction.transaction_api.TransactionFeatureApi
import soft.divan.financemanager.presenter.MainViewModel
import soft.divan.financemanager.presenter.navigation.BottomNavigationBar
import soft.divan.financemanager.presenter.navigation.NavGraph
import soft.divan.financemanager.presenter.navigation.ScreenBottom
import soft.divan.financemanager.string.R
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun MainScreenPreview() {
    FinanceManagerTheme {
/*
        MainScreen(settingsRoute = SettingsRouter. )
*/
    }
}

@SuppressLint("FlowOperatorInvokedInComposition")
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel(),
    splashFeatureApi: SplashScreenFeatureApi,
    expenseFeatureApi: ExpensesFeatureApi,
    incomeFeatureApi: IncomeFeatureApi,
    accountFeatureApi: AccountFeatureApi,
    categoryFeatureApi: CategoryFeatureApi,
    settingsFeatureApi: SettingsFeatureApi,
    transactionFeatureApi: TransactionFeatureApi,
) {
    val navController = rememberNavController()
    val currentDestination by navController.currentBackStackEntryAsState()
    val showBars = currentDestination?.destination?.route != splashFeatureApi.splashScreenRoute
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    val bottomRoutes = ScreenBottom.items.map { it.route }

// если текущий маршрут НЕ входит в нижнюю навигацию, сохраняем последний выбранный из нижнего меню
    val effectiveRoute = if (currentRoute in bottomRoutes) {
        currentRoute
    } else {
        bottomRoutes.firstOrNull { it in navController.previousBackStackEntry?.destination?.route.orEmpty() }
            ?: bottomRoutes.first()
    }

    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val notConnectedMessage = stringResource(R.string.not_connected)
    LaunchedEffect(isOffline) {
        if (isOffline) {
            snackbarHostState.showSnackbar(
                message = notConnectedMessage,
                duration = Indefinite,
            )
        }
    }

    Column(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.weight(1f)
        ) {
            NavGraph(
                navController = navController,
                splashScreenFeatureApi = splashFeatureApi,
                incomeFeatureApi = incomeFeatureApi,
                expenseFeatureApi = expenseFeatureApi,
                categoryFeatureApi = categoryFeatureApi,
                accountFeatureApi = accountFeatureApi,
                settingsFeatureApi = settingsFeatureApi,
                transactionFeatureApi = transactionFeatureApi
            )

            SnackbarHost(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 72.dp),
                hostState = snackbarHostState,
            )

        }
        if (showBars) {
            BottomNavigationBar(
                modifier = Modifier,
                navController = navController,
                currentRoute = effectiveRoute
            )
        }

    }

}




