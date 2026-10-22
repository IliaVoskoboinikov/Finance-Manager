package soft.divan.financemanager.presenter.theme

import android.app.UiModeManager
import soft.divan.financemanager.feature.designapp.impl.domain.model.ThemeMode

/**
 * Тёмная ли тема для выбранного режима.
 *
 * @param isSystemDark тёмная ли сейчас тема системы — учитывается только для [ThemeMode.SYSTEM].
 */
fun ThemeMode.isDarkTheme(isSystemDark: Boolean): Boolean = when (this) {
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
    ThemeMode.SYSTEM -> isSystemDark
}

/**
 * Режим для `UiModeManager.setApplicationNightMode` (API 31+).
 *
 * Система запоминает его между запусками процесса и применяет к системному сплэшу и
 * конфигурации ещё до того, как прочитан DataStore, — поэтому холодный старт сразу
 * в выбранной теме. [ThemeMode.SYSTEM] — `MODE_NIGHT_AUTO`, то есть «как в системе».
 */
fun ThemeMode.toApplicationNightMode(): Int = when (this) {
    ThemeMode.LIGHT -> UiModeManager.MODE_NIGHT_NO
    ThemeMode.DARK -> UiModeManager.MODE_NIGHT_YES
    ThemeMode.SYSTEM -> UiModeManager.MODE_NIGHT_AUTO
}
