package soft.divan.financemanager.core.data.vault

import android.util.Log
import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.domain.model.LocalDataState
import soft.divan.financemanager.core.security.keyset.BiometricEnrollment
import soft.divan.financemanager.core.security.keyset.BiometricEnvelope
import soft.divan.financemanager.core.security.keyset.DekEnvelope
import soft.divan.financemanager.core.security.keyset.KeyLevel
import soft.divan.financemanager.core.security.keyset.Keyset
import soft.divan.financemanager.core.security.keystore.KeyUnavailableException
import java.security.GeneralSecurityException
import javax.inject.Inject

/** [LocalDataBiometrics] поверх хранилища ключей. */
class BiometricVault @Inject constructor(
    private val core: VaultCore,
    private val envelope: DekEnvelope,
    private val biometricEnvelope: BiometricEnvelope
) : LocalDataBiometrics {

    override fun observeEnabled(): Flow<Boolean> = core.store.observeBiometric()

    /**
     * Непригодная копия (чаще всего система инвалидировала ключ после нового отпечатка)
     * удаляется. Это не повод стирать данные: остаётся вход по PIN, биометрию можно включить
     * заново.
     */
    override suspend fun startUnlock(): BiometricSession? = core.exclusive {
        val keyset = pinKeyset()
        val copy = keyset?.biometric
        if (keyset == null || copy == null) {
            null
        } else {
            try {
                BiometricSession(biometricEnvelope.unlockCipher(keyset), copy.alias, dek = null)
            } catch (e: KeyUnavailableException) {
                Log.w(TAG, "Biometric copy is unusable and removed", e)
                replaceKeyset(keyset, keyset.withoutBiometric())
                null
            }
        }
    }

    /**
     * Сбой на этом шаге копию **не** удаляет: инвалидацию ключа Keystore показывает уже
     * подготовка шифра в [startUnlock], а здесь отказ означает сбой аутентификации — повторить
     * можно, а стёртую копию вернуть нельзя.
     */
    override suspend fun finishUnlock(session: BiometricSession): Boolean = core.exclusive {
        val keyset = pinKeyset()
        if (session.closed || keyset?.biometric?.alias != session.alias) {
            false
        } else {
            session.closed = true
            try {
                core.unlockDatabase(biometricEnvelope.finishUnlock(session.cipher, keyset))
            } catch (e: KeyUnavailableException) {
                Log.w(TAG, "Biometric unlock failed", e)
                false
            }
        }
    }

    override suspend fun startEnrollment(pin: String): BiometricEnrollmentStart {
        val pinChars = pin.toCharArray()
        return try {
            core.exclusive {
                val keyset = pinKeyset()
                if (keyset == null) {
                    BiometricEnrollmentStart.Unavailable
                } else {
                    prepareEnrollment(
                        keyset,
                        pinChars
                    )
                }
            }
        } finally {
            pinChars.fill(Char.MIN_VALUE)
        }
    }

    override suspend fun finishEnrollment(session: BiometricSession): Boolean = core.exclusive {
        val keyset = pinKeyset()
        val dek = session.dek
        if (session.closed || keyset == null || dek == null) {
            false
        } else {
            session.closed = true
            try {
                val copy = biometricEnvelope.finishEnrollment(
                    BiometricEnrollment(session.alias, session.cipher),
                    dek
                )
                core.commit(previous = keyset, next = keyset.withBiometric(copy))
                Log.i(TAG, "Biometric unlock enabled")
                true
            } catch (e: GeneralSecurityException) {
                Log.e(TAG, "Biometric copy could not be sealed", e)
                core.deleteKey(session.alias)
                false
            } finally {
                dek.fill(0)
            }
        }
    }

    override suspend fun cancel(session: BiometricSession) = core.exclusive {
        if (!session.closed) {
            session.closed = true
            session.dek?.let { dek ->
                dek.fill(0)
                // Ключ включения ещё не попал в набор — без сессии он никому не нужен
                core.deleteKey(session.alias)
            }
        }
    }

    override suspend fun disable() = core.exclusive {
        val keyset = pinKeyset()
        if (keyset?.biometric != null) {
            replaceKeyset(keyset, keyset.withoutBiometric())
            Log.i(TAG, "Biometric unlock disabled")
        }
    }

    /** PIN подтверждает владельца и нужен, чтобы достать ключ базы для биометрической копии. */
    private fun prepareEnrollment(keyset: Keyset, pin: CharArray): BiometricEnrollmentStart {
        val unsealed = runCatching { envelope.openWithPin(keyset, pin) }
        val dek = unsealed.getOrNull()
        if (unsealed.isFailure) Log.e(TAG, "PIN-level key is unavailable", unsealed.exceptionOrNull())
        return when {
            unsealed.isFailure -> BiometricEnrollmentStart.Unavailable

            dek == null -> BiometricEnrollmentStart.WrongPin

            else -> try {
                val enrollment = biometricEnvelope.enrollmentCipher()
                BiometricEnrollmentStart.Ready(
                    BiometricSession(enrollment.cipher, enrollment.alias, dek)
                )
            } catch (e: KeyUnavailableException) {
                Log.e(TAG, "Biometric key could not be created", e)
                dek.fill(0)
                BiometricEnrollmentStart.Unavailable
            }
        }
    }

    private suspend fun replaceKeyset(current: Keyset, next: Keyset) {
        core.commit(previous = current, next = next)
        if (core.state.value is LocalDataState.Locked) {
            core.publish(LocalDataState.Locked(biometricEnabled = next.biometric != null))
        }
    }

    private suspend fun pinKeyset(): Keyset? =
        core.readKeysetOrNull()?.takeIf { it.level == KeyLevel.PIN }

    private companion object {
        const val TAG = "BiometricVault"
    }
}
