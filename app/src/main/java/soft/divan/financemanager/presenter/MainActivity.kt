package soft.divan.financemanager.presenter

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.auth.domain.usecase.GetAuthStatusUseCase
import soft.divan.financemanager.core.domain.model.LocalDataState
import soft.divan.financemanager.core.domain.usecase.ObserveLocalDataStateUseCase
import soft.divan.financemanager.core.featureapi.FeatureApi
import soft.divan.financemanager.core.notifications.fcm.PushSubscriptionManager
import soft.divan.financemanager.core.notifications.scheduler.InactivityReminderScheduler
import soft.divan.financemanager.feature.auth.api.AuthFeatureApi
import soft.divan.financemanager.feature.designapp.impl.domain.model.ThemeMode
import soft.divan.financemanager.feature.designapp.impl.domain.usecase.GetAccentColorUseCase
import soft.divan.financemanager.feature.designapp.impl.domain.usecase.GetCustomAccentColorUseCase
import soft.divan.financemanager.feature.designapp.impl.domain.usecase.GetThemeModeUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.ObservePinSetUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.ObserveSecureScreenUseCase
import soft.divan.financemanager.feature.security.impl.presenter.screen.KeyLostScreen
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
    lateinit var observePinSet: ObservePinSetUseCase

    @Inject
    lateinit var observeLocalDataState: ObserveLocalDataStateUseCase

    @Inject
    lateinit var observeSecureScreen: ObserveSecureScreenUseCase

    @Inject
    lateinit var inactivityReminderScheduler: InactivityReminderScheduler

    @Inject
    lateinit var pushSubscriptionManager: PushSubscriptionManager

    /** Пройден ли замок приложения — см. [AppLockViewModel]. */
    private val appLock: AppLockViewModel by viewModels()

    // «Установлен ли PIN» — из наблюдаемого потока. Раньше значение читалось синхронно прямо
    // в composition (disk I/O + crypto на главном потоке на каждой рекомпозиции), потом —
    // перечитывалось по жизненному циклу; поток же сразу сообщает, что PIN сняло стирание данных
    // («забыл PIN», исчерпанные попытки, восстановление после потери ключа), и замок не требует
    // PIN, которого уже нет.
    // null — ещё не прочитано: пока так, главный экран не строится, иначе он мелькал бы до замка.
    private val isPinSet = mutableStateOf<Boolean?>(null)

    // Сброс синхронный: состояние читается гейтом напрямую, без эффекта, который отработал бы
    // только на следующем кадре и дал бы главному экрану мелькнуть при возвращении.
    private val autoLockObserver = LifecycleEventObserver { _, event ->
        if (event == Lifecycle.Event.ON_STOP) appLock.lock()
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

    private fun observePinSetState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                observePinSet().collect { set ->
                    // PIN, созданный в настройках при открытом приложении, не запирает его сразу:
                    // пользователь только что ввёл этот PIN дважды, а замок сбросил бы его экран
                    if (isPinSet.value == false && set) appLock.unlock()
                    isPinSet.value = set
                }
            }
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

    /**
     * Защита экрана (`FLAG_SECURE`) включена по умолчанию и ставится **до** первого кадра: иначе
     * превью в списке недавних успело бы сняться, пока настройка читается с диска. Если
     * пользователь её выключил, флаг снимется, как только настройка прочитается.
     *
     * Диалоги Compose — отдельные окна; они наследуют флаг (`SecureFlagPolicy.Inherit`).
     */
    private fun applySecureScreen() {
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                observeSecureScreen().collect { secure ->
                    if (secure) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        applySecureScreen()

        observePinSetState()
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

            FinanceManagerTheme(
                darkTheme = isDark,
                accentColor = accentColor,
                customColor = customColor
            ) {
                LocalDataGate()
            }
        }
    }

    /**
     * Что показать, зависит прежде всего от того, доступны ли данные: главный экран строится только
     * при открытой базе. Раньше гейт держался на кешированном `isPinSet`, который стартует с
     * `false`, — главный экран успевал обратиться к базе до показа замка; для запертой базы
     * уровня PIN это было бы падением.
     */
    @Composable
    private fun LocalDataGate() {
        val dataState by observeLocalDataState().collectAsState()
        val onUnlocked = appLock::unlock

        val pinSet = isPinSet.value
        when {
            dataState == LocalDataState.KeyLost -> KeyLostScreen()

            dataState is LocalDataState.Locked -> PinLockScreen(onUnlocked = onUnlocked)

            dataState == LocalDataState.Initializing || pinSet == null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )

            pinSet && !appLock.unlocked -> PinLockScreen(onUnlocked = onUnlocked)

            else -> RootNavDisplay(
                splashFeatureApi = splashFeatureApi,
                authFeatureApi = authFeatureApi,
                getAuthStatusUseCase = getAuthStatusUseCase,
                mainScreen = {
                    MainScreen(features = features)
                }
            )
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
