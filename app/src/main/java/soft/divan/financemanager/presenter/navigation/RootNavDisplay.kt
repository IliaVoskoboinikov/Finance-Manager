package soft.divan.financemanager.presenter.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.auth.domain.model.AuthStatus
import soft.divan.financemanager.core.auth.domain.usecase.GetAuthStatusUseCase
import soft.divan.financemanager.feature.auth.api.AuthFeatureApi
import soft.divan.financemanager.feature.auth.api.AuthKey
import soft.divan.financemanager.feature.splashscreen.api.SplashKey
import soft.divan.financemanager.feature.splashscreen.api.SplashScreenFeatureApi

/**
 * Корневой граф приложения: старт → авторизация или главный экран.
 *
 * Это отдельный back stack: у вкладок нижней навигации свои стеки внутри [MainKey].
 */
@Composable
fun RootNavDisplay(
    splashFeatureApi: SplashScreenFeatureApi,
    authFeatureApi: AuthFeatureApi,
    getAuthStatusUseCase: GetAuthStatusUseCase,
    mainScreen: @Composable () -> Unit
) {
    val backStack = rememberNavBackStack(SplashKey)
    val coroutineScope = rememberCoroutineScope()

    val authStatus by getAuthStatusUseCase().collectAsState(initial = null)

    // Реактивно следим за статусом авторизации: разлогин очищает весь стек
    // и уводит на экран авторизации.
    LaunchedEffect(authStatus) {
        if (authStatus == AuthStatus.UNAUTHORIZED) {
            backStack.replaceAll(AuthKey)
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeAt(backStack.lastIndex) },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            splashFeatureApi.registerEntries(
                scope = this,
                onFinish = {
                    coroutineScope.launch {
                        val currentStatus = getAuthStatusUseCase().first()
                        backStack.replaceAll(
                            if (currentStatus == AuthStatus.UNAUTHORIZED) AuthKey else MainKey
                        )
                    }
                }
            )

            authFeatureApi.registerRootEntries(
                scope = this,
                onAuthSuccess = { backStack.replaceAll(MainKey) }
            )

            entry<MainKey> {
                mainScreen()
            }
        }
    )
}

/** Заменяет весь стек единственным экраном [key] — аналог `popUpTo(0) { inclusive = true }`. */
private fun NavBackStack<NavKey>.replaceAll(key: NavKey) {
    clear()
    add(key)
}
