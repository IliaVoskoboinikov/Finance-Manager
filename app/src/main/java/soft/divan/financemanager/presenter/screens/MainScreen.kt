package soft.divan.financemanager.presenter.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration.Indefinite
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import soft.divan.financemanager.R
import soft.divan.financemanager.feature.account.account_impl.AccountFeatureApi
import soft.divan.financemanager.feature.analysis.analysis_api.AnalysisFeatureApi
import soft.divan.financemanager.feature.category.category_api.CategoryFeatureApi
import soft.divan.financemanager.feature.design_app.design_app_api.DesignAppFeatureApi
import soft.divan.financemanager.feature.expenses.expenses_api.ExpensesFeatureApi
import soft.divan.financemanager.feature.income.income_api.IncomeFeatureApi
import soft.divan.financemanager.feature.security.security_api.SecurityFeatureApi
import soft.divan.financemanager.feature.settings.settings_api.SettingsFeatureApi
import soft.divan.financemanager.feature.splash_screen.splash_screen_api.SplashScreenFeatureApi
import soft.divan.financemanager.feature.transaction.transaction_api.TransactionFeatureApi
import soft.divan.financemanager.presenter.MainViewModel
import soft.divan.financemanager.presenter.navigation.BottomNavigationBar
import soft.divan.financemanager.presenter.navigation.NavGraph
import soft.divan.financemanager.presenter.navigation.ScreenBottom
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
    securityFeatureApi: SecurityFeatureApi,
    designAppFeatureApi: DesignAppFeatureApi,
    analysisFeatureApi: AnalysisFeatureApi
) {
    val navController = rememberNavController()
    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    val bottomScreens = ScreenBottom.items(
        expenses = expenseFeatureApi,
        income = incomeFeatureApi,
        account = accountFeatureApi,
        category = categoryFeatureApi,
        settings = settingsFeatureApi
    )

    val bottomRoutes = bottomScreens.map { it.feature.route }

    val selectedBottomRoute = rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(currentRoute) {
        if (currentRoute in bottomRoutes) {
            selectedBottomRoute.value = currentRoute
        }
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

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.background)
        ) {
            NavGraph(
                navController = navController,
                splashScreenFeatureApi = splashFeatureApi,
                incomeFeatureApi = incomeFeatureApi,
                expenseFeatureApi = expenseFeatureApi,
                categoryFeatureApi = categoryFeatureApi,
                accountFeatureApi = accountFeatureApi,
                settingsFeatureApi = settingsFeatureApi,
                transactionFeatureApi = transactionFeatureApi,
                securityFeatureApi = securityFeatureApi,
                designAppFeatureApi = designAppFeatureApi,
                analysisFeatureApi = analysisFeatureApi,
            )

            SnackbarHost(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 72.dp),
                hostState = snackbarHostState,
            )
        }

        if (currentRoute != splashFeatureApi.route) {
            BottomNavigationBar(
                modifier = Modifier,
                navController = navController,
                screens = bottomScreens,
                currentRoute = selectedBottomRoute.value
            )
        }
    }
}



