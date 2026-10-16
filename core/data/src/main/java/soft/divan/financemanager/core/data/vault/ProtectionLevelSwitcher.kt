package soft.divan.financemanager.core.data.vault

import android.util.Log
import soft.divan.financemanager.core.domain.model.ProtectionChangeResult
import soft.divan.financemanager.core.security.keyset.DekEnvelope
import soft.divan.financemanager.core.security.keyset.KeyLevel
import soft.divan.financemanager.core.security.keyset.Keyset
import soft.divan.financemanager.core.security.keyset.PinAttempts
import soft.divan.financemanager.core.security.keystore.KeyUnavailableException
import java.io.IOException
import javax.inject.Inject

/**
 * Смена уровня защиты и PIN: ключ базы перезаворачивается, сама база не трогается.
 *
 * Это 32 байта, а не `PRAGMA rekey` всего файла, поэтому смена мгновенная и не зависит от объёма
 * данных. Протокол замены набора — см. [VaultCore].
 *
 * Ошибка PIN здесь **не учитывается** политикой попыток: эти операции доступны только из
 * открытого приложения, где данные уже доступны, а стирать их из-за опечатки в настройках было
 * бы наказанием без пользы.
 */
class ProtectionLevelSwitcher @Inject constructor(
    private val core: VaultCore,
    private val envelope: DekEnvelope
) {

    /**
     * Перезаворачивает ключ базы под уровень [target]. [pin] нужен, если текущий или новый
     * уровень — [KeyLevel.PIN]. Уходя с уровня PIN, набор теряет и биометрическую копию.
     */
    suspend fun changeLevel(target: KeyLevel, pin: CharArray?): ProtectionChangeResult = try {
        core.exclusive { rewrap(target, pin) }
    } finally {
        pin?.fill(Char.MIN_VALUE)
    }

    /**
     * Меняет PIN уровня [KeyLevel.PIN]: внутренний слой заворачивается новым PIN, биометрическая
     * копия переносится как есть — от PIN она не зависит. На других уровнях ключ от PIN не
     * зависит, и менять нечего.
     */
    suspend fun changePin(currentPin: CharArray, newPin: CharArray): ProtectionChangeResult = try {
        core.exclusive { rewrapPin(currentPin, newPin) }
    } finally {
        currentPin.fill(Char.MIN_VALUE)
        newPin.fill(Char.MIN_VALUE)
    }

    private suspend fun rewrap(target: KeyLevel, pin: CharArray?): ProtectionChangeResult = guarded {
        val current = core.store.read() ?: throw KeyUnavailableException("No keyset")
        if (current.level == target) return@guarded ProtectionChangeResult.Changed
        if ((current.level == KeyLevel.PIN || target == KeyLevel.PIN) && pin == null) {
            return@guarded ProtectionChangeResult.WrongPin
        }
        val dek = unseal(current, pin) ?: return@guarded ProtectionChangeResult.WrongPin
        try {
            val next = when (target) {
                KeyLevel.OPEN -> envelope.sealOpen(dek)
                KeyLevel.DEVICE -> envelope.sealDevice(dek)
                KeyLevel.PIN -> envelope.sealPin(dek, requireNotNull(pin))
            }
            replace(current, next, dek, pin)
            // Новый уровень начинается с чистого учёта ошибок
            core.store.writeAttempts(PinAttempts())
            Log.i(TAG, "Protection level changed: ${current.level} -> $target")
            ProtectionChangeResult.Changed
        } finally {
            dek.fill(0)
        }
    }

    private suspend fun rewrapPin(
        currentPin: CharArray,
        newPin: CharArray
    ): ProtectionChangeResult = guarded {
        val current = core.store.read() ?: throw KeyUnavailableException("No keyset")
        if (current.level != KeyLevel.PIN) return@guarded ProtectionChangeResult.Changed
        val dek = envelope.openWithPin(current, currentPin)
            ?: return@guarded ProtectionChangeResult.WrongPin
        try {
            replace(current, envelope.sealPin(dek, newPin, biometric = current.biometric), dek, newPin)
            ProtectionChangeResult.Changed
        } finally {
            dek.fill(0)
        }
    }

    private suspend fun replace(current: Keyset, next: Keyset, dek: ByteArray, pin: CharArray?) {
        core.verify(next, dek, pin, previous = current)
        core.commit(previous = current, next = next)
    }

    private fun unseal(keyset: Keyset, pin: CharArray?): ByteArray? =
        if (keyset.level == KeyLevel.PIN) {
            envelope.openWithPin(keyset, requireNotNull(pin))
        } else {
            envelope.openWithoutUser(keyset)
        }

    /**
     * Отказ Keystore, испорченный набор или сбой записи — операция не состоялась, прежний набор
     * цел: он заменяется только атомарной записью уже проверенного нового.
     */
    private inline fun guarded(block: () -> ProtectionChangeResult): ProtectionChangeResult = try {
        block()
    } catch (e: KeyUnavailableException) {
        Log.e(TAG, "Keyset change failed", e)
        ProtectionChangeResult.Failed
    } catch (e: IOException) {
        Log.e(TAG, "Keyset could not be written", e)
        ProtectionChangeResult.Failed
    }

    private companion object {
        const val TAG = "ProtectionLevel"
    }
}
