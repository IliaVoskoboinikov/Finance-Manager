package soft.divan.financemanager.core.data.repository

import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import soft.divan.financemanager.core.auth.domain.model.AuthStatus
import soft.divan.financemanager.core.auth.domain.provider.AuthStateProvider
import soft.divan.financemanager.core.data.mapper.toDomain
import soft.divan.financemanager.core.data.mapper.toKeyLevel
import soft.divan.financemanager.core.data.outbox.OutboxProcessor
import soft.divan.financemanager.core.data.source.OutboxLocalDataSource
import soft.divan.financemanager.core.data.vault.ProtectionLevelSwitcher
import soft.divan.financemanager.core.data.vault.VaultCore
import soft.divan.financemanager.core.domain.model.DataProtectionLevel
import soft.divan.financemanager.core.domain.model.ProtectionChangeResult
import soft.divan.financemanager.core.domain.repository.DataProtectionRepository
import javax.inject.Inject

/**
 * [DataProtectionRepository] поверх хранилища ключей.
 *
 * Досылка очереди перед уровнем PIN — решение на стыке ключей, сессии и outbox, которого само
 * хранилище не знает.
 */
class DataProtectionRepositoryImpl @Inject constructor(
    private val switcher: ProtectionLevelSwitcher,
    private val core: VaultCore,
    private val authStateProvider: AuthStateProvider,
    private val outboxProcessor: OutboxProcessor,
    private val outboxLocalDataSource: OutboxLocalDataSource
) : DataProtectionRepository {

    override fun observeLevel(): Flow<DataProtectionLevel> =
        core.store.observeLevel().filterNotNull().map { it.toDomain() }

    override suspend fun currentLevel(): DataProtectionLevel? =
        core.readKeysetOrNull()?.level?.toDomain()

    override suspend fun changeLevel(
        target: DataProtectionLevel,
        pin: String?
    ): ProtectionChangeResult =
        if (target == DataProtectionLevel.PIN && hasUnsentChanges()) {
            ProtectionChangeResult.UnsentChanges
        } else {
            switcher.changeLevel(target.toKeyLevel(), pin?.toCharArray())
        }

    override suspend fun changePin(currentPin: String, newPin: String): ProtectionChangeResult =
        switcher.changePin(currentPin.toCharArray(), newPin.toCharArray())

    /**
     * На уровне PIN фоновой синхронизации нет, поэтому перед ним очередь досылается. У гостя
     * сервера нет — досылать нечего. Сбой досылки (нет сети, база) — тоже «не отправлено»:
     * включать уровень, не убедившись в обратном, нельзя.
     */
    @Suppress("TooGenericExceptionCaught")
    private suspend fun hasUnsentChanges(): Boolean {
        if (authStateProvider.observeStatus().first() != AuthStatus.AUTHORIZED) return false
        return try {
            outboxProcessor.process()
            outboxLocalDataSource.countUnsent() > 0
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "Outbox could not be drained", e)
            true
        }
    }

    private companion object {
        const val TAG = "DataProtection"
    }
}
