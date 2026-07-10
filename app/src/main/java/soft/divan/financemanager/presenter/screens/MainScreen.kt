package soft.divan.financemanager.presenter.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import soft.divan.financemanager.R
import soft.divan.financemanager.feature.category.api.CategoryFeatureApi
import soft.divan.financemanager.feature.myaccounts.impl.MyAccountsFeatureApi
import soft.divan.financemanager.feature.settings.api.SettingsFeatureApi
import soft.divan.financemanager.feature.transactionstoday.api.TransactionsTodayFeatureApi
import soft.divan.financemanager.presenter.MainViewModel
import soft.divan.financemanager.presenter.navigation.BottomNavigationBar
import soft.divan.financemanager.presenter.navigation.NavGraph
import soft.divan.financemanager.presenter.navigation.ScreenBottom
import soft.divan.financemanager.uikit.icons.Calculator
import soft.divan.financemanager.uikit.icons.Chart90
import soft.divan.financemanager.uikit.icons.Downtrend
import soft.divan.financemanager.uikit.icons.Settings
import soft.divan.financemanager.uikit.icons.Uptrend
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme

/**
 * Корневой экран приложения после авторизации: собирает пункты нижней навигации
 * из [FeatureApi][soft.divan.financemanager.core.featureapi.FeatureApi] фич и
 * хостит их граф внутри [MainScreenContent].
 */
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel(),
    transactionsTodayFeatureApi: TransactionsTodayFeatureApi,
    categoryFeatureApi: CategoryFeatureApi,
    settingsFeatureApi: SettingsFeatureApi,
    myAccountsFeatureApi: MyAccountsFeatureApi
) {
    val bottomScreens = ScreenBottom.items(
        transactionsToday = transactionsTodayFeatureApi,
        myAccounts = myAccountsFeatureApi,
        category = categoryFeatureApi,
        settings = settingsFeatureApi
    )

    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
// todo
    /*  val notConnectedMessage = stringResource(R.string.not_connected)
      LaunchedEffect(isOffline) {
          if (isOffline) {
              snackbarHostState.showSnackbar(
                  message = notConnectedMessage,
                  duration = Indefinite
              )
          }
      }*/

    MainScreenContent(
        bottomScreens = bottomScreens,
        snackbarHostState = snackbarHostState,
        hapticToggleMenu = viewModel::hapticToggleMenu,
        modifier = modifier
    ) { navController ->
        NavGraph(
            navController = navController,
            transactionsTodayFeatureApi = transactionsTodayFeatureApi,
            categoryFeatureApi = categoryFeatureApi,
            settingsFeatureApi = settingsFeatureApi,
            myAccountsFeatureApi = myAccountsFeatureApi
        )
    }
}

@Composable
internal fun MainScreenContent(
    bottomScreens: List<ScreenBottom>,
    snackbarHostState: SnackbarHostState,
    hapticToggleMenu: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (NavHostController) -> Unit
) {
    val navController = rememberNavController()

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.background)
        ) {
            content(navController)

            SnackbarHost(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 72.dp),
                hostState = snackbarHostState
            )
        }

        BottomNavigationBar(
            modifier = Modifier,
            navController = navController,
            screens = bottomScreens,
            hapticToggleMenu = hapticToggleMenu
        )
    }
}

private fun previewBottomScreens() = listOf(
    ScreenBottom("expenses", R.string.expenses, Icons.Filled.Downtrend),
    ScreenBottom("income", R.string.income, Icons.Filled.Uptrend),
    ScreenBottom("accounts", R.string.account, Icons.Filled.Calculator),
    ScreenBottom("categories", R.string.category, Icons.Filled.Chart90),
    ScreenBottom("settings", R.string.settings, Icons.Filled.Settings)
)

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MainScreenPreview() {
    val screens = previewBottomScreens()
    FinanceManagerTheme {
        MainScreenContent(
            bottomScreens = screens,
            snackbarHostState = remember { SnackbarHostState() },
            hapticToggleMenu = {}
        ) { navController ->
            // Реальный NavHost с экранами-заглушками: дефолтный таб попадает
            // в back stack и подсвечивается, контент показывает текущий экран.
            NavHost(
                navController = navController,
                startDestination = screens.first().route
            ) {
                screens.forEach { screen ->
                    composable(screen.route) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(screen.title),
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }
            }
        }
    }
}
