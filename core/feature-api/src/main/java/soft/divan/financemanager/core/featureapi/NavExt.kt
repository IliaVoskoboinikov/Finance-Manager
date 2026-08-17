package soft.divan.financemanager.core.featureapi

import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

/**
 * Добавляет экраны [featureApi] в собираемый `entryProvider`.
 *
 * Синтаксический сахар над [FeatureApi.registerEntries], чтобы сборка графа в `app`
 * читалась как список фич.
 */
fun EntryProviderScope<NavKey>.register(
    featureApi: FeatureApi,
    navigator: Navigator,
    modifier: Modifier = Modifier
) {
    featureApi.registerEntries(
        scope = this,
        navigator = navigator,
        modifier = modifier
    )
}
