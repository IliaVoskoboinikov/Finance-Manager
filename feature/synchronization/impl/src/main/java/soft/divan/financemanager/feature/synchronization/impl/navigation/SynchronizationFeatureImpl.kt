package soft.divan.financemanager.feature.synchronization.impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import soft.divan.financemanager.core.featureapi.Navigator
import soft.divan.financemanager.feature.synchronization.api.SynchronizationFeatureApi
import soft.divan.financemanager.feature.synchronization.api.SynchronizationKey
import soft.divan.financemanager.feature.synchronization.impl.precenter.screen.SynchronizationScreen
import javax.inject.Inject

class SynchronizationFeatureImpl @Inject constructor() : SynchronizationFeatureApi {

    override fun registerEntries(
        scope: EntryProviderScope<NavKey>,
        navigator: Navigator,
        modifier: Modifier
    ) {
        scope.entry<SynchronizationKey> {
            SynchronizationScreen(
                modifier = modifier,
                onNavigateBack = navigator::back
            )
        }
    }
}
