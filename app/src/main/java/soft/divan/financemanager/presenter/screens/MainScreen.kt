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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    transactionsTodayFeatureApi: TransactionsTodayFeatureApi,
    categoryFeatureApi: CategoryFeatureApi,
    settingsFeatureApi: SettingsFeatureApi,
    myAccountsFeatureApi: MyAccountsFeatureApi
) {
    val navController = rememberNavController()

    val bottomScreens = ScreenBottom.items(
        transactionsToday = transactionsTodayFeatureApi,
        myAccounts = myAccountsFeatureApi,
        category = categoryFeatureApi,
        settings = settingsFeatureApi
    )

    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val notConnectedMessage = stringResource(R.string.not_connected)
    LaunchedEffect(isOffline) {
        if (isOffline) {
            snackbarHostState.showSnackbar(
                message = notConnectedMessage,
                duration = Indefinite
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
                transactionsTodayFeatureApi = transactionsTodayFeatureApi,
                categoryFeatureApi = categoryFeatureApi,
                settingsFeatureApi = settingsFeatureApi,
                myAccountsFeatureApi = myAccountsFeatureApi
            )

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
            hapticToggleMenu = viewModel::hapticToggleMenu
        )
    }
}
