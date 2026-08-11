package soft.divan.financemanager.feature.haptics.impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import soft.divan.financemanager.core.featureapi.Navigator
import soft.divan.financemanager.feature.haptics.api.HapticsFeatureApi
import soft.divan.financemanager.feature.haptics.api.HapticsKey
import soft.divan.financemanager.feature.haptics.impl.precenter.screen.HapticsScreen
import javax.inject.Inject

class HapticFeatureImpl @Inject constructor() : HapticsFeatureApi {

    override fun registerEntries(
        scope: EntryProviderScope<NavKey>,
        navigator: Navigator,
        modifier: Modifier
    ) {
        scope.entry<HapticsKey> {
            HapticsScreen(
                modifier = modifier,
                onNavigateBack = navigator::back
            )
        }
    }
}
