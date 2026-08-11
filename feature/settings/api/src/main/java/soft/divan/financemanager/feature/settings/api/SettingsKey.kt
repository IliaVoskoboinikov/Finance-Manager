package soft.divan.financemanager.feature.settings.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Экран настроек — вкладка нижней навигации. */
@Serializable
data object SettingsKey : NavKey

/** Экран «О программе» — внутренний экран настроек. */
@Serializable
data object AboutTheProgramKey : NavKey
