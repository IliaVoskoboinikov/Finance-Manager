package soft.divan.financemanager.feature.category.impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import soft.divan.financemanager.core.featureapi.Navigator
import soft.divan.financemanager.feature.category.api.CategoryFeatureApi
import soft.divan.financemanager.feature.category.api.CategoryKey
import soft.divan.financemanager.feature.category.impl.presenter.screen.CategoriesScreen
import javax.inject.Inject

class CategoryFeatureImpl @Inject constructor() : CategoryFeatureApi {

    override fun registerEntries(
        scope: EntryProviderScope<NavKey>,
        navigator: Navigator,
        modifier: Modifier
    ) {
        scope.entry<CategoryKey> {
            CategoriesScreen(
                modifier = modifier
            )
        }
    }
}
