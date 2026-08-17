package soft.divan.financemanager.feature.auth.impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import soft.divan.financemanager.core.featureapi.Navigator
import soft.divan.financemanager.feature.auth.api.AuthFeatureApi
import soft.divan.financemanager.feature.auth.api.AuthKey
import soft.divan.financemanager.feature.auth.api.ProfileAuthKey
import soft.divan.financemanager.feature.auth.api.ProfileKey
import soft.divan.financemanager.feature.auth.impl.presenter.screen.AuthScreen
import soft.divan.financemanager.feature.auth.impl.presenter.screen.ProfileScreen
import javax.inject.Inject

class AuthFeatureImpl @Inject constructor() : AuthFeatureApi {

    /**
     * Вложенный сценарий: профиль и открытая из него повторная авторизация.
     * После успешного входа просто возвращаемся в профиль.
     */
    override fun registerEntries(
        scope: EntryProviderScope<NavKey>,
        navigator: Navigator,
        modifier: Modifier
    ) {
        scope.entry<ProfileKey> {
            ProfileScreen(
                onNavigateToAuth = { navigator.goTo(ProfileAuthKey) }
            )
        }

        scope.entry<ProfileAuthKey> {
            AuthScreen(
                onAuthSuccess = navigator::back
            )
        }
    }

    /**
     * Корневой сценарий: авторизация как точка входа в приложение.
     * Куда уходить после успешного входа, решает хост.
     */
    override fun registerRootEntries(
        scope: EntryProviderScope<NavKey>,
        onAuthSuccess: () -> Unit,
        modifier: Modifier
    ) {
        scope.entry<AuthKey> {
            AuthScreen(
                onAuthSuccess = onAuthSuccess
            )
        }
    }
}
