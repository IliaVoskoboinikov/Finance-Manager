package soft.divan.financemanager.feature.languages.impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import soft.divan.financemanager.core.featureapi.Navigator
import soft.divan.financemanager.feature.languages.api.LanguagesFeatureApi
import soft.divan.financemanager.feature.languages.api.LanguagesKey
import soft.divan.financemanager.feature.languages.impl.precenter.screen.LanguagesScreen
import javax.inject.Inject

class LanguagesFeatureImpl @Inject constructor() : LanguagesFeatureApi {

    override fun registerEntries(
        scope: EntryProviderScope<NavKey>,
        navigator: Navigator,
        modifier: Modifier
    ) {
        scope.entry<LanguagesKey> {
            LanguagesScreen(
                modifier = modifier,
                onNavigateBack = navigator::back
            )
        }
    }
}
