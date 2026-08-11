package soft.divan.financemanager.feature.security.impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import soft.divan.financemanager.core.featureapi.Navigator
import soft.divan.financemanager.feature.security.api.CreatePinKey
import soft.divan.financemanager.feature.security.api.SecurityFeatureApi
import soft.divan.financemanager.feature.security.api.SecurityKey
import soft.divan.financemanager.feature.security.impl.presenter.screen.CreatePinScreen
import soft.divan.financemanager.feature.security.impl.presenter.screen.SecurityScreen
import javax.inject.Inject

class SecurityFeatureImpl @Inject constructor() : SecurityFeatureApi {

    override fun registerEntries(
        scope: EntryProviderScope<NavKey>,
        navigator: Navigator,
        modifier: Modifier
    ) {
        scope.entry<SecurityKey> {
            SecurityScreen(
                modifier = modifier,
                onNavigateBack = navigator::back,
                onNavigateToCreatePin = { navigator.goTo(CreatePinKey) }
            )
        }

        scope.entry<CreatePinKey> {
            CreatePinScreen(
                onNavigateBack = navigator::back
            )
        }
    }
}
