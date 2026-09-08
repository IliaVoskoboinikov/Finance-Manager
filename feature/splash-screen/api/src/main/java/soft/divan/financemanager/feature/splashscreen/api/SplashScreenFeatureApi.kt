package soft.divan.financemanager.feature.splashscreen.api

import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey

/**
 * Фича стартового экрана.
 *
 * Не реализует `FeatureApi`: экран живёт только в корневом графе и сообщает о завершении
 * анимации коллбэком — куда уходить дальше, решает хост (зависит от статуса авторизации).
 */
interface SplashScreenFeatureApi {

    /**
     * Регистрирует стартовый экран в корневом `entryProvider`.
     *
     * @param scope билдер `entryProvider` корневого графа.
     * @param onFinish вызывается по завершении стартовой анимации.
     * @param modifier модификатор корня экрана.
     */
    fun registerEntries(
        scope: EntryProviderScope<NavKey>,
        onFinish: () -> Unit,
        modifier: Modifier = Modifier
    )
}
