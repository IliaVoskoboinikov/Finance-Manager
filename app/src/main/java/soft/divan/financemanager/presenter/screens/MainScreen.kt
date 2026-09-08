package soft.divan.financemanager.presenter.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import com.github.skydoves.navgraph.annotations.NavDestination
import com.github.skydoves.navgraph.annotations.NavEdge
import com.github.skydoves.navgraph.annotations.NavPreview
import soft.divan.financemanager.core.featureapi.FeatureApi
import soft.divan.financemanager.feature.category.api.CategoryKey
import soft.divan.financemanager.feature.myaccounts.impl.MyAccountsKey
import soft.divan.financemanager.feature.settings.api.SettingsKey
import soft.divan.financemanager.feature.transactionstoday.api.TransactionsTodayKey
import soft.divan.financemanager.presenter.MainViewModel
import soft.divan.financemanager.presenter.navigation.MainKey
import soft.divan.financemanager.presenter.navigation.BottomNavigationBar
import soft.divan.financemanager.presenter.navigation.MainNavDisplay
import soft.divan.financemanager.presenter.navigation.ScreenBottom
import soft.divan.financemanager.presenter.navigation.TopLevelBackStack
import soft.divan.financemanager.presenter.navigation.rememberTopLevelBackStack
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme

/**
 * Корневой экран приложения после авторизации: рисует нижнюю навигацию и хостит
 * граф всех фич ([features]) внутри [MainScreenContent].
 */
@NavDestination(route = MainKey::class)
@NavEdge(to = TransactionsTodayKey::class, label = "вкладка «Расходы» / «Доходы»")
@NavEdge(to = MyAccountsKey::class, label = "вкладка «Счёт»")
@NavEdge(to = CategoryKey::class, label = "вкладка «Статьи»")
@NavEdge(to = SettingsKey::class, label = "вкладка «Настройки»")
@Composable
fun MainScreen(
    features: Set<FeatureApi>,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel()
) {
    val bottomScreens = remember { ScreenBottom.items() }

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
    ) { backStack ->
        MainNavDisplay(
            features = features,
            backStack = backStack
        )
    }
}

@Composable
internal fun MainScreenContent(
    bottomScreens: List<ScreenBottom>,
    snackbarHostState: SnackbarHostState,
    hapticToggleMenu: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (TopLevelBackStack) -> Unit
) {
    val backStack = rememberTopLevelBackStack(bottomScreens)

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .weight(1f)
                .background(MaterialTheme.colorScheme.background)
        ) {
            content(backStack)

            SnackbarHost(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 72.dp),
                hostState = snackbarHostState
            )
        }

        BottomNavigationBar(
            modifier = Modifier,
            backStack = backStack,
            screens = bottomScreens,
            hapticToggleMenu = hapticToggleMenu
        )
    }
}

@NavPreview(route = MainKey::class, primary = true)
@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MainScreenPreview() {
    val screens = ScreenBottom.items()
    FinanceManagerTheme {
        MainScreenContent(
            bottomScreens = screens,
            snackbarHostState = remember { SnackbarHostState() },
            hapticToggleMenu = {}
        ) { backStack ->
            // Экраны-заглушки вместо графа фич: подсветка вкладки и содержимое
            // берутся из того же TopLevelBackStack, что и в приложении.
            val currentTab = screens.first { it.key == backStack.currentTabKey }
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(currentTab.title),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}
