package soft.divan.financemanager.feature.security.impl.presenter.model

import androidx.compose.runtime.Immutable
import soft.divan.financemanager.feature.security.impl.domain.model.PinLockStatus
import javax.crypto.Cipher

/**
 * Состояние экрана замка.
 *
 * @property status Сколько попыток осталось и не запрещён ли ввод.
 * @property error Что показать под точками ввода.
 * @property biometricEnabled Можно ли предложить биометрию (на уровне PIN — если есть её копия).
 * @property dataProtectedByPin Замок открывает данные, а не только интерфейс: от этого зависит
 *   текст предупреждения «забыл PIN».
 * @property isGuest Данные только на устройстве — стирание необратимо.
 * @property showForgotPinDialog Показан диалог «забыл PIN».
 * @property inProgress Идёт проверка PIN или стирание — ввод заблокирован.
 */
@Immutable
data class PinLockUiState(
    val status: PinLockStatus = PinLockStatus.UNLIMITED,
    val error: PinLockError? = null,
    val biometricEnabled: Boolean = false,
    val dataProtectedByPin: Boolean = false,
    val isGuest: Boolean = false,
    val showForgotPinDialog: Boolean = false,
    val inProgress: Boolean = false
)

/** Сообщение под полем ввода PIN. */
@Immutable
sealed interface PinLockError {

    /** Неверный PIN; [attemptsLeft] — сколько ошибок осталось до стирания (`null` — без лимита). */
    data class WrongPin(val attemptsLeft: Int?) : PinLockError

    /** Ввод запрещён — пауза после череды ошибок. */
    data object LockedOut : PinLockError

    /** Биометрия больше недоступна: в системе изменились отпечатки. Остался вход по PIN. */
    data object BiometricInvalidated : PinLockError
}

/** Что написать под точками ввода; строку по нему подбирает экран. */
@Immutable
sealed interface PinLockMessage {

    /** Писать нечего. */
    data object None : PinLockMessage

    /** Идёт пауза: ввод снова доступен через [secondsLeft] секунд. */
    data class LockedOut(val secondsLeft: Long) : PinLockMessage

    /** Неверный PIN; [attemptsLeft] — сколько ошибок осталось до стирания (`null` — без лимита). */
    data class WrongPin(val attemptsLeft: Int?) : PinLockMessage

    /** Следующая ошибка сотрёт данные. */
    data object LastAttempt : PinLockMessage

    /** Вход по отпечатку отключён: в системе изменились отпечатки. */
    data object BiometricInvalidated : PinLockMessage
}

/**
 * Сообщение под точками ввода, когда до конца паузы [lockoutSecondsLeft] секунд.
 *
 * Предупреждение о последней попытке опирается на учёт попыток, а не на последнюю ошибку:
 * девятая ошибка всегда ставит паузу, и к её концу — как и при повторном показе замка — ошибки в
 * состоянии уже нет. Без этого пользователь не узнал бы, что следующий неверный PIN сотрёт данные.
 */
fun PinLockUiState.message(lockoutSecondsLeft: Long): PinLockMessage {
    val error = error
    return when {
        lockoutSecondsLeft > 0 -> PinLockMessage.LockedOut(lockoutSecondsLeft)
        error is PinLockError.BiometricInvalidated -> PinLockMessage.BiometricInvalidated
        error is PinLockError.WrongPin -> PinLockMessage.WrongPin(error.attemptsLeft)
        status.isLastAttempt -> PinLockMessage.LastAttempt
        else -> PinLockMessage.None
    }
}

/** Одноразовые события экрана замка. */
sealed interface PinLockEvent {

    /** Можно входить. */
    data object Unlocked : PinLockEvent

    /** Данные стёрты (забыт PIN или исчерпаны попытки) — дальше экран входа. */
    data object Wiped : PinLockEvent

    /** Показать системный запрос биометрии; [cipher] — для `CryptoObject` или `null`. */
    class ShowBiometricPrompt(val cipher: Cipher?) : PinLockEvent
}
