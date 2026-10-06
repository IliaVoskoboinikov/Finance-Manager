package soft.divan.financemanager.feature.security.impl.domain.model

import java.time.Instant

/**
 * Сколько ещё можно ошибиться с PIN.
 *
 * @property attemptsLeft Ошибок до стирания данных; `null` — ограничения нет: ниже уровня PIN
 *   это только замок интерфейса, данные им не защищены.
 * @property lockedUntil До какого момента ввод запрещён; `null` — вводить можно.
 */
data class PinLockStatus(
    val attemptsLeft: Int? = null,
    val lockedUntil: Instant? = null
) {
    /** Следующая ошибка сотрёт данные. */
    val isLastAttempt: Boolean get() = attemptsLeft == 1

    companion object {
        /** Замок без счётчика попыток. */
        val UNLIMITED = PinLockStatus()
    }
}

/** Итог ввода PIN на экране замка. */
sealed interface PinCheckResult {

    /** PIN верный — можно входить. */
    data object Correct : PinCheckResult

    /** PIN неверный. */
    data class Wrong(val status: PinLockStatus) : PinCheckResult

    /** Ввод запрещён до [PinLockStatus.lockedUntil]; PIN не проверялся. */
    data class LockedOut(val status: PinLockStatus) : PinCheckResult

    /** Попытки исчерпаны: данные стёрты, выполнен выход из аккаунта. */
    data object Wiped : PinCheckResult

    /** Ключ данных недоступен. */
    data object KeyLost : PinCheckResult
}

/** Итог удаления PIN. */
enum class DeletePinResult {
    DELETED,
    WRONG_PIN,

    /** На уровне PIN им завёрнут ключ данных: сначала нужно понизить уровень. */
    NOT_ALLOWED
}
