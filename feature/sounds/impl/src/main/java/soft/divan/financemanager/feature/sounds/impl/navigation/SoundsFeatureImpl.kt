package soft.divan.financemanager.feature.sounds.impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import soft.divan.financemanager.core.featureapi.Navigator
import soft.divan.financemanager.feature.sounds.api.SoundsFeatureApi
import soft.divan.financemanager.feature.sounds.api.SoundsKey
import soft.divan.financemanager.feature.sounds.impl.precenter.screen.SoundsScreen
import javax.inject.Inject

class SoundsFeatureImpl @Inject constructor() : SoundsFeatureApi {

    override fun registerEntries(
        scope: EntryProviderScope<NavKey>,
        navigator: Navigator,
        modifier: Modifier
    ) {
        scope.entry<SoundsKey> {
            SoundsScreen(
                modifier = modifier,
                onNavigateBack = navigator::back
            )
        }
    }
}
