package soft.divan.financemanager.feature.designapp.impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import soft.divan.financemanager.core.featureapi.Navigator
import soft.divan.financemanager.feature.designapp.api.DesignAppFeatureApi
import soft.divan.financemanager.feature.designapp.api.DesignAppKey
import soft.divan.financemanager.feature.designapp.impl.precenter.screen.DesignAppScreen
import javax.inject.Inject

class DesignAppFeatureImpl @Inject constructor() : DesignAppFeatureApi {

    override fun registerEntries(
        scope: EntryProviderScope<NavKey>,
        navigator: Navigator,
        modifier: Modifier
    ) {
        scope.entry<DesignAppKey> {
            DesignAppScreen(
                modifier = modifier
            )
        }
    }
}
