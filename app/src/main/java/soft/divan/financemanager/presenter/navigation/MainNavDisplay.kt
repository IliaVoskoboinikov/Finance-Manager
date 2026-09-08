package soft.divan.financemanager.presenter.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import soft.divan.financemanager.core.featureapi.FeatureApi
import soft.divan.financemanager.core.featureapi.register

/**
 * Граф экранов под нижней навигацией.
 *
 * Экраны собираются из [features] — все фичи, доступные после авторизации, добавляют свои
 * `NavEntry` в общий `entryProvider`. Показывается стек текущей вкладки ([TopLevelBackStack]).
 *
 * Переходы отключены: как и на Navigation 2, переключение вкладок и открытие вложенных
 * экранов происходят без анимации.
 */
@Composable
fun MainNavDisplay(
    features: Set<FeatureApi>,
    backStack: TopLevelBackStack,
    modifier: Modifier = Modifier
) {
    NavDisplay(
        backStack = backStack.displayStack,
        modifier = modifier,
        onBack = { backStack.back() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        transitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        popTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        predictivePopTransitionSpec = { EnterTransition.None togetherWith ExitTransition.None },
        entryProvider = entryProvider<NavKey> {
            features.forEach { feature ->
                register(featureApi = feature, navigator = backStack)
            }
        }
    )
}
