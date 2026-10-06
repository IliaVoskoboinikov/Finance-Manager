package soft.divan.financemanager.feature.security.impl.domain.model

import soft.divan.financemanager.core.domain.model.DataProtectionLevel

/**
 * Всё, что показывает экран настроек безопасности.
 *
 * @property level Уровень защиты данных.
 * @property hasPin Задан ли PIN приложения.
 * @property biometricEnabled Есть ли биометрическая копия ключа (только на уровне PIN).
 * @property secureScreen Включена ли защита экрана (`FLAG_SECURE`).
 * @property isGuest Гостевой режим: данные только на устройстве, стирание необратимо.
 */
data class SecuritySettings(
    val level: DataProtectionLevel,
    val hasPin: Boolean,
    val biometricEnabled: Boolean,
    val secureScreen: Boolean,
    val isGuest: Boolean
)
