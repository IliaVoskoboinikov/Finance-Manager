package soft.divan.financemanager.core.data.vault

import android.util.Log
import soft.divan.financemanager.core.domain.model.LocalDataState
import soft.divan.financemanager.core.domain.model.PinLockout
import soft.divan.financemanager.core.domain.model.PinUnlockResult
import soft.divan.financemanager.core.security.keyset.DekEnvelope
import soft.divan.financemanager.core.security.keyset.KeyLevel
import soft.divan.financemanager.core.security.keyset.Keyset
import soft.divan.financemanager.core.security.keyset.PinAttemptPolicy
import soft.divan.financemanager.core.security.keyset.PinAttempts
import javax.inject.Inject

/**
 * Разблокировка данных уровня PIN: проверка PIN — это разворачивание ключа базы, тег GCM и есть
 * проверка. Хеша PIN на этом пути нет вовсе.
 *
 * Ошибка записывается **до** проверки: иначе процесс можно было бы убивать в момент проверки и
 * перебирать PIN без штрафа. Верный PIN запись стирает; недоступный ключ Keystore её отменяет —
 * PIN при этом не проверялся. Стирание после исчерпания попыток
 * выполняет вызывающий ([VaultPinResult.Exhausted]) — с выходом из аккаунта, вне блокировки
 * хранилища.
 */
class PinUnlocker @Inject constructor(
    private val core: VaultCore,
    private val envelope: DekEnvelope,
    private val policy: PinAttemptPolicy
) {

    /** Проверяет PIN и открывает базу; массив [pin] затирается. */
    suspend fun unlockWithPin(pin: CharArray): VaultPinResult = try {
        core.exclusive { checkPin(pin) }
    } finally {
        pin.fill(Char.MIN_VALUE)
    }

    /** Сколько попыток осталось и не запрещён ли ввод сейчас. */
    suspend fun pinLockout(): PinLockout = core.exclusive { lockout(core.store.readAttempts()) }

    private suspend fun checkPin(pin: CharArray): VaultPinResult {
        val keyset = core.readKeysetOrNull()
        return when {
            keyset == null -> done(keyLost())
            keyset.level != KeyLevel.PIN -> done(PinUnlockResult.NotRequired)
            else -> checkAttempts(keyset, pin)
        }
    }

    private suspend fun checkAttempts(keyset: Keyset, pin: CharArray): VaultPinResult {
        val attempts = core.store.readAttempts()
        return when {
            // Десятая ошибка паузы не ставит — дальше только стирание
            policy.isExhausted(attempts) -> VaultPinResult.Exhausted

            policy.lockedUntil(attempts) != null -> done(PinUnlockResult.LockedOut(lockout(attempts)))

            else -> verify(keyset, pin, attempts)
        }
    }

    private suspend fun verify(keyset: Keyset, pin: CharArray, attempts: PinAttempts): VaultPinResult {
        val failed = policy.afterFailure(attempts)
        core.store.writeAttempts(failed)
        val unsealed = runCatching { envelope.openWithPin(keyset, pin) }
        val dek = unsealed.getOrNull()
        return when {
            unsealed.isFailure -> {
                Log.e(TAG, "PIN-level key is unavailable", unsealed.exceptionOrNull())
                // Слой Keystore не снялся — до PIN проверка не дошла, и ошибку возвращаем: сбой
                // Keystore бывает временным, и «Попробовать снова» не должно вести к паузам и
                // стиранию. Подбору это не помогает — без внешнего слоя PIN не проверить.
                core.store.writeAttempts(attempts)
                done(keyLost())
            }

            dek != null -> done(
                if (core.unlockDatabase(dek)) PinUnlockResult.Success else PinUnlockResult.KeyLost
            )

            policy.isExhausted(failed) -> VaultPinResult.Exhausted

            else -> done(PinUnlockResult.WrongPin(lockout(failed)))
        }
    }

    private fun keyLost(): PinUnlockResult {
        core.publish(LocalDataState.KeyLost)
        return PinUnlockResult.KeyLost
    }

    private fun lockout(attempts: PinAttempts) = PinLockout(
        attemptsLeft = policy.attemptsLeft(attempts),
        lockedUntil = policy.lockedUntil(attempts)
    )

    private fun done(result: PinUnlockResult) = VaultPinResult.Done(result)

    private companion object {
        const val TAG = "PinUnlocker"
    }
}

/** Итог проверки PIN внутри хранилища: исчерпание попыток обрабатывает вызывающий. */
sealed interface VaultPinResult {

    /** Проверка завершена с результатом [result]. */
    data class Done(val result: PinUnlockResult) : VaultPinResult

    /** Попытки исчерпаны: данные нужно стереть с выходом из аккаунта. */
    data object Exhausted : VaultPinResult
}
