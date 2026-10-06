package soft.divan.financemanager.core.domain.model

import java.time.Instant

/**
 * Доступны ли сейчас локальные данные.
 *
 * Главный экран строится только в [Open]: в остальных состояниях база закрыта, и любое обращение
 * к ней завершится ошибкой.
 */
sealed interface LocalDataState {

    /** Ключи ещё читаются — ненадолго при старте процесса. */
    data object Initializing : LocalDataState

    /** База открыта. */
    data object Open : LocalDataState

    /**
     * Уровень [DataProtectionLevel.PIN]: база закрыта до ввода PIN или биометрии.
     *
     * @property biometricEnabled Есть ли биометрическая копия ключа — показывать ли кнопку.
     */
    data class Locked(val biometricEnabled: Boolean) : LocalDataState

    /**
     * Ключ базы недоступен (ключ Keystore утерян или инвалидирован, набор ключей испорчен).
     * Данные прочитать нельзя; выход — стереть их и начать заново.
     */
    data object KeyLost : LocalDataState
}

/**
 * Сколько ещё можно ошибиться с PIN и не запрещён ли ввод прямо сейчас.
 *
 * @property attemptsLeft Сколько ошибок осталось до стирания данных.
 * @property lockedUntil До какого момента ввод запрещён; `null` — вводить можно.
 */
data class PinLockout(
    val attemptsLeft: Int,
    val lockedUntil: Instant? = null
)
