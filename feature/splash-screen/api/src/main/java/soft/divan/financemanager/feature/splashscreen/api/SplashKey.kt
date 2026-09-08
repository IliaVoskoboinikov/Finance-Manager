package soft.divan.financemanager.feature.splashscreen.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Стартовый экран приложения, с которого начинается корневой back stack. */
@Serializable
data object SplashKey : NavKey
