package soft.divan.financemanager.presenter

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.core.content.ContextCompat
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import soft.divan.financemanager.core.auth.domain.usecase.GetAuthStatusUseCase
import soft.divan.financemanager.core.featureapi.FeatureApi
import soft.divan.financemanager.core.notifications.fcm.PushSubscriptionManager
import soft.divan.financemanager.core.notifications.scheduler.InactivityReminderScheduler
import soft.divan.financemanager.feature.auth.api.AuthFeatureApi
import soft.divan.financemanager.feature.designapp.impl.domain.model.ThemeMode
import soft.divan.financemanager.feature.designapp.impl.domain.usecase.GetAccentColorUseCase
import soft.divan.financemanager.feature.designapp.impl.domain.usecase.GetCustomAccentColorUseCase
import soft.divan.financemanager.feature.designapp.impl.domain.usecase.GetThemeModeUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.IsPinSetUseCase
import soft.divan.financemanager.feature.security.impl.presenter.screen.PinLockScreen
import soft.divan.financemanager.feature.splashscreen.api.SplashScreenFeatureApi
import soft.divan.financemanager.presenter.navigation.RootNavDisplay
import soft.divan.financemanager.presenter.screens.MainScreen
import soft.divan.financemanager.uikit.theme.AccentColor
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var splashFeatureApi: SplashScreenFeatureApi

    @Inject
    lateinit var authFeatureApi: AuthFeatureApi

    /** Все фичи основного графа — см. `FeatureNavigationModule`. */
    @Inject
    lateinit var features: Set<@JvmSuppressWildcards FeatureApi>

    @Inject
    lateinit var getAuthStatusUseCase: GetAuthStatusUseCase

    @Inject
    lateinit var getThemeModeUseCase: GetThemeModeUseCase

    @Inject
    lateinit var getAccentColorUseCase: GetAccentColorUseCase

    @Inject
    lateinit var getCustomAccentColorUseCase: GetCustomAccentColorUseCase

    @Inject
    lateinit var isPinSetUseCase: IsPinSetUseCase

    @Inject
    lateinit var inactivityReminderScheduler: InactivityReminderScheduler

    @Inject
    lateinit var pushSubscriptionManager: PushSubscriptionManager

    private val shouldLock = mutableStateOf(false)

    // Кешируем «установлен ли PIN». Раньше значение читалось синхронно прямо
    // в composition (disk I/O + crypto на главном потоке на каждой рекомпозиции).
    // Теперь читаем вне главного потока и обновляем по жизненному циклу.
    private val isPinSet = mutableStateOf(false)

    private val autoLockObserver = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> refreshPinSet()

            Lifecycle.Event.ON_STOP -> {
                if (isPinSet.value) {
                    shouldLock.value = true
                }
            }

            else -> Unit
        }
    }

    /**
     * ON_START у `ProcessLifecycleOwner` — это выход приложения на передний план, то есть
     * реальное присутствие пользователя. Именно от него отсчитывается неактивность.
     *
     */
    private val inactivityObserver = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_START) {
            inactivityReminderScheduler.onUserActive()
        }
    }

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            // Отказ — штатный сценарий: NotificationHelper сам молча пропустит показ.
        }

    private fun refreshPinSet() {
        lifecycleScope.launch {
            isPinSet.value = withContext(Dispatchers.IO) { isPinSetUseCase() }
        }
    }

    /** С Android 13 показ уведомлений требует runtime-разрешения. */
    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        refreshPinSet()
        requestNotificationPermissionIfNeeded()
        // Здесь, а не в App.onCreate(): к моменту старта Activity FirebaseApp гарантированно
        // поднят своим ContentProvider'ом, и подписка не зависит от фоновых стартов процесса.
        pushSubscriptionManager.subscribeToBroadcasts()
        ProcessLifecycleOwner.get().lifecycle.addObserver(autoLockObserver)
        ProcessLifecycleOwner.get().lifecycle.addObserver(inactivityObserver)

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
                if (isPinSet.value && !isPinVerified) {
                    PinLockScreen(onPinCorrect = {
                        isPinVerified = true
                        shouldLock.value = false
                    })
                } else {
                    RootNavDisplay(
                        splashFeatureApi = splashFeatureApi,
                        authFeatureApi = authFeatureApi,
                        getAuthStatusUseCase = getAuthStatusUseCase,
                        mainScreen = {
                            MainScreen(features = features)
                        }
                    )
                }
            }
        }
    }

    /**
     * На API < 33 AppCompat при объявленных configChanges применяет новую локаль,
     * вызывая только Activity.onConfigurationChanged — по view-иерархии событие
     * не рассылается (на API 33+ это делает система через ViewRootImpl). Без него
     * AndroidComposeView не инвалидирует LocalConfiguration, и stringResource()
     * вне изменившегося стейта (например, подписи нижнего меню) не перерисовывается.
     * Рассылаем конфигурацию по иерархии вручную.
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            window.decorView.dispatchConfigurationChanged(newConfig)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ProcessLifecycleOwner.get().lifecycle.removeObserver(autoLockObserver)
        ProcessLifecycleOwner.get().lifecycle.removeObserver(inactivityObserver)
    }
}
