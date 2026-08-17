package soft.divan.financemanager.feature.splashscreen.impl.navigation

import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import soft.divan.financemanager.feature.splashscreen.api.SplashKey
import soft.divan.financemanager.feature.splashscreen.api.SplashScreenFeatureApi
import soft.divan.financemanager.feature.splashscreen.impl.screens.SplashScreen
import javax.inject.Inject

class SplashScreenFeatureImpl @Inject constructor() : SplashScreenFeatureApi {

    override fun registerEntries(
        scope: EntryProviderScope<NavKey>,
        onFinish: () -> Unit,
        modifier: Modifier
    ) {
        scope.entry<SplashKey> {
            SplashScreen(
                onFinish = onFinish
            )
        }
    }
}
