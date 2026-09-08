package soft.divan.financemanager.feature.auth.api

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

/**
 * Экран авторизации как точка входа в приложение.
 *
 * Живёт в корневом back stack: успешная авторизация уводит на главный экран,
 * поэтому ключ отличается от [ProfileAuthKey].
 */
@Serializable
data object AuthKey : NavKey

/** Экран профиля. Открывается из настроек. */
@Serializable
data object ProfileKey : NavKey

/** Экран авторизации, открытый из профиля: после успеха возвращает назад, в профиль. */
@Serializable
data object ProfileAuthKey : NavKey
