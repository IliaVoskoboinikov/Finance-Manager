package soft.divan.financemanager.presenter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.AndroidEntryPoint
import soft.divan.financemanager.feature.category.api.CategoryFeatureApi
import soft.divan.financemanager.feature.designapp.impl.domain.model.ThemeMode
import soft.divan.financemanager.feature.designapp.impl.domain.usecase.GetAccentColorUseCase
import soft.divan.financemanager.feature.designapp.impl.domain.usecase.GetCustomAccentColorUseCase
import soft.divan.financemanager.feature.designapp.impl.domain.usecase.GetThemeModeUseCase
import soft.divan.financemanager.feature.myaccounts.impl.MyAccountsFeatureApi
import soft.divan.financemanager.feature.security.impl.domain.usecase.IsPinSetUseCase
import soft.divan.financemanager.feature.security.impl.presenter.screen.PinLockScreen
import soft.divan.financemanager.feature.settings.api.SettingsFeatureApi
import soft.divan.financemanager.feature.splashscreen.api.SplashScreenFeatureApi
import soft.divan.financemanager.feature.transactionstoday.api.TransactionsTodayFeatureApi
import soft.divan.financemanager.presenter.navigation.RootNavGraph
import soft.divan.financemanager.presenter.screens.MainScreen
import soft.divan.financemanager.uikit.theme.AccentColor
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var splashFeatureApi: SplashScreenFeatureApi

    @Inject
    lateinit var transactionsTodayFeatureApi: TransactionsTodayFeatureApi

    @Inject
    lateinit var myAccountsFeatureApi: MyAccountsFeatureApi

    @Inject
    lateinit var categoryFeatureApi: CategoryFeatureApi

    @Inject
    lateinit var settingsFeatureApi: SettingsFeatureApi

    @Inject
    lateinit var getThemeModeUseCase: GetThemeModeUseCase

    @Inject
    lateinit var getAccentColorUseCase: GetAccentColorUseCase

    @Inject
    lateinit var getCustomAccentColorUseCase: GetCustomAccentColorUseCase

    @Inject
    lateinit var isPinSetUseCase: IsPinSetUseCase

    private val shouldLock = mutableStateOf(false)

    private val autoLockObserver = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_STOP -> {
                if (isPinSetUseCase()) {
                    shouldLock.value = true
                }
            }

            else -> Unit
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ProcessLifecycleOwner.get().lifecycle.addObserver(autoLockObserver)

        setContent {
            val themeMode by getThemeModeUseCase().collectAsState(initial = ThemeMode.LIGHT)
            val isDark = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            val accentColor by getAccentColorUseCase().collectAsState(initial = AccentColor.MINT)
            val customColor = getCustomAccentColorUseCase().collectAsState(initial = null).value

            var isPinVerified by rememberSaveable { mutableStateOf(false) }

            LaunchedEffect(shouldLock.value) {
                if (shouldLock.value) {
                    isPinVerified = false
                }
            }

            FinanceManagerTheme(
                darkTheme = isDark,
                accentColor = accentColor,
                customColor = customColor
            ) {
                if (isPinSetUseCase() && !isPinVerified) {
                    PinLockScreen(onPinCorrect = {
                        isPinVerified = true
                        shouldLock.value = false
                    })
                } else {
                    RootNavGraph(
                        splashFeatureApi = splashFeatureApi,
                        mainScreen = {
                            MainScreen(
                                transactionsTodayFeatureApi = transactionsTodayFeatureApi,
                                myAccountsFeatureApi = myAccountsFeatureApi,
                                categoryFeatureApi = categoryFeatureApi,
                                settingsFeatureApi = settingsFeatureApi
                            )
                        }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(autoLockObserver)
    }
}
// Revue me>>
