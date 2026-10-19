package soft.divan.financemanager.core.data.repository

import android.util.Log
import kotlinx.coroutines.flow.StateFlow
import soft.divan.financemanager.core.auth.domain.model.AuthEvent
import soft.divan.financemanager.core.auth.domain.provider.AuthStateProvider
import soft.divan.financemanager.core.data.vault.LocalDataVault
import soft.divan.financemanager.core.data.vault.PinUnlocker
import soft.divan.financemanager.core.data.vault.VaultCore
import soft.divan.financemanager.core.data.vault.VaultPinResult
import soft.divan.financemanager.core.domain.model.LocalDataState
import soft.divan.financemanager.core.domain.model.PinLockout
import soft.divan.financemanager.core.domain.model.PinUnlockResult
import soft.divan.financemanager.core.domain.repository.LocalDataAccessRepository
import soft.divan.financemanager.core.security.keyset.KeyLevel
import javax.inject.Inject

/**
 * [LocalDataAccessRepository] поверх хранилища ключей.
 *
 * Здесь сходятся ключи и сессия: стирание с выходом из аккаунта — решение на стыке, которого само
 * хранилище не знает.
 */
class LocalDataAccessRepositoryImpl @Inject constructor(
    private val vault: LocalDataVault,
    private val unlocker: PinUnlocker,
    private val core: VaultCore,
    private val authStateProvider: AuthStateProvider
) : LocalDataAccessRepository {

    override val state: StateFlow<LocalDataState> get() = vault.state

    override suspend fun initialize() = vault.initialize()

    override suspend fun unlockWithPin(pin: String): PinUnlockResult =
        when (val result = unlocker.unlockWithPin(pin.toCharArray())) {
            is VaultPinResult.Done -> result.result

            VaultPinResult.Exhausted -> {
                Log.w(TAG, "PIN attempts exhausted, wiping local data")
                signOutAndWipe()
                PinUnlockResult.Wiped
            }
        }

    override suspend fun pinLockout(): PinLockout = unlocker.pinLockout()

    override suspend fun lock() = vault.lock()

    override suspend fun wipe(signOut: Boolean) {
        if (signOut) signOutAndWipe() else vault.wipe()
    }

    /**
     * Данные уровня L1 после стирания вернутся с сервера по сохранённой сессии — это и есть
     * восстановление. Данные под PIN так возвращать нельзя: иначе потеря ключа стала бы обходом
     * PIN. Неизвестный уровень (набор ключей не читается) трактуется так же осторожно.
     */
    override suspend fun recover(): Boolean {
        val level = core.readKeysetOrNull()?.level
        val signOut = level == null || level == KeyLevel.PIN
        wipe(signOut)
        return signOut
    }

    /**
     * Выход из аккаунта сам стирает данные — через `DatabaseCleanupManager`. Вызывать его можно
     * только снаружи блокировки хранилища, иначе стирание ждало бы само себя.
     */
    private suspend fun signOutAndWipe() {
        authStateProvider.sendEvent(AuthEvent.OnSessionExpired)
    }

    private companion object {
        const val TAG = "LocalDataAccess"
    }
}
