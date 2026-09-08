package soft.divan.financemanager.feature.security.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/** Экран настроек безопасности (PIN-код, биометрия). */
@Serializable
data object SecurityKey : NavKey

/** Экран создания PIN-кода — внутренний экран настроек безопасности. */
@Serializable
data object CreatePinKey : NavKey
