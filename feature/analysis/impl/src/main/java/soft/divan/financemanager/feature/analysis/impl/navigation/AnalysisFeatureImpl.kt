package soft.divan.financemanager.feature.analysis.impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import soft.divan.financemanager.core.featureapi.Navigator
import soft.divan.financemanager.feature.analysis.api.AnalysisFeatureApi
import soft.divan.financemanager.feature.analysis.api.AnalysisKey
import soft.divan.financemanager.feature.analysis.impl.precenter.screen.AnalysisScreen
import javax.inject.Inject

class AnalysisFeatureImpl @Inject constructor() : AnalysisFeatureApi {

    override fun registerEntries(
        scope: EntryProviderScope<NavKey>,
        navigator: Navigator,
        modifier: Modifier
    ) {
        scope.entry<AnalysisKey> { analysisKey ->
            AnalysisScreen(
                modifier = modifier,
                isIncome = analysisKey.isIncome,
                onNavigateBack = navigator::back
            )
        }
    }
}
