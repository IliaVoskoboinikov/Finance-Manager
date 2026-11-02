package soft.divan.financemanager.core.data.vault

import kotlinx.coroutines.flow.Flow
import javax.crypto.Cipher

/**
 * Биометрия уровня PIN: ключ базы выдаёт сам Keystore после строгой биометрии.
 *
 * Операция двухшаговая, потому что между шагами — системный `BiometricPrompt`:
 * 1. `start…` готовит шифр ([BiometricSession.cipher]); слой представления отдаёт его в
 *    `BiometricPrompt.CryptoObject`;
 * 2. после успешной аутентификации шифр разблокирован, и `finish…` завершает операцию.
 *
 * Ключ базы в слой представления не попадает: сессия непрозрачна, наружу виден только шифр.
 * Неудачную или отменённую сессию нужно закрыть через [cancel].
 */
interface LocalDataBiometrics {

    /** Есть ли биометрическая копия ключа. */
    fun observeEnabled(): Flow<Boolean>

    /**
     * Сессия разблокировки или `null`, если копии нет или она инвалидирована: новый отпечаток в
     * системе делает ключ непригодным. Инвалидированная копия удаляется — остаётся вход по PIN,
     * данные не стираются.
     */
    suspend fun startUnlock(): BiometricSession?

    /**
     * Открывает базу; `false` — ключ не развернулся (копия остаётся: повторить можно) или база
     * не открылась (тогда данные в состоянии «ключ утерян»).
     */
    suspend fun finishUnlock(session: BiometricSession): Boolean

    /**
     * Сессия включения биометрии. PIN подтверждает владельца и нужен, чтобы достать ключ базы;
     * ошибка здесь не учитывается политикой попыток — данные в этот момент и так открыты.
     */
    suspend fun startEnrollment(pin: String): BiometricEnrollmentStart

    /** Сохраняет биометрическую копию; `false` — Keystore отказал. */
    suspend fun finishEnrollment(session: BiometricSession): Boolean

    /** Закрывает неудавшуюся сессию: затирает ключ и удаляет несохранённый ключ Keystore. */
    suspend fun cancel(session: BiometricSession)

    /** Удаляет биометрическую копию. */
    suspend fun disable()
}

/**
 * Незавершённая биометрическая операция.
 *
 * @property cipher Шифр для `BiometricPrompt.CryptoObject`.
 */
class BiometricSession internal constructor(
    val cipher: Cipher,
    internal val alias: String,
    internal val dek: ByteArray?
) {
    /** Сессия закрыта — завершена или отменена; повторно не используется. */
    @Volatile
    internal var closed: Boolean = false
}

/** Итог начала включения биометрии. */
sealed interface BiometricEnrollmentStart {

    /** Шифр готов — можно показывать `BiometricPrompt`. */
    data class Ready(val session: BiometricSession) : BiometricEnrollmentStart

    /** PIN неверный. */
    data object WrongPin : BiometricEnrollmentStart

    /** Биометрия недоступна: уровень не PIN или Keystore не создал ключ. */
    data object Unavailable : BiometricEnrollmentStart
}
