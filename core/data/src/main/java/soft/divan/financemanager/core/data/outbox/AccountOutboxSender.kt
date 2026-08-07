package soft.divan.financemanager.core.data.outbox

import com.google.gson.Gson
import soft.divan.financemanager.core.data.dto.AccountDto
import soft.divan.financemanager.core.data.dto.CreateAccountRequestDto
import soft.divan.financemanager.core.data.dto.UpdateAccountRequestDto
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.financemanager.core.data.source.AccountRemoteDataSource
import soft.divan.financemanager.core.database.entity.OutboxEntryEntity
import soft.divan.financemanager.core.database.model.OutboxOperation
import soft.divan.financemanager.core.database.model.SyncStatus
import soft.divan.financemanager.core.domain.model.AccountStatus
import javax.inject.Inject

/**
 * Отправляет операции над счетами из очереди и отражает результат в локальной БД.
 *
 * Устроен так же, как [TransactionOutboxSender], но удаление у счетов не всегда означает
 * исчезновение строки: сервер может перевести счёт в архив, и тогда локальная запись остаётся —
 * иначе история операций не смогла бы показать его имя и валюту (см. `docs/account-archive.md`).
 */
class AccountOutboxSender @Inject constructor(
    private val remoteDataSource: AccountRemoteDataSource,
    private val localDataSource: AccountLocalDataSource,
    private val gson: Gson
) : OutboxSender {

    override suspend fun send(entry: OutboxEntryEntity): OutboxSendResult =
        when (entry.operation) {
            OutboxOperation.CREATE -> create(entry)
            OutboxOperation.UPDATE -> update(entry)
            OutboxOperation.DELETE -> delete(entry)
        }

    /** Создание с read-back: неуспех может означать «уже создано» при потерянном ACK. */
    private suspend fun create(entry: OutboxEntryEntity): OutboxSendResult {
        val request = gson.fromJson(entry.payload, CreateAccountRequestDto::class.java)
        val outcome = outboxCall(entry.operation) { remoteDataSource.create(request) }

        return when (outcome) {
            is OutboxCallOutcome.Ok -> confirm(entry, outcome.body)

            // Заблокированная сеть до сервера не дошла — перепроверять нечего
            is OutboxCallOutcome.Rejected -> when (outcome.result) {
                is OutboxSendResult.Blocked -> outcome.result
                else -> readBack(entry, outcome.result)
            }
        }
    }

    private suspend fun readBack(
        entry: OutboxEntryEntity,
        original: OutboxSendResult
    ): OutboxSendResult {
        val outcome = outboxCall(entry.operation) { remoteDataSource.getById(entry.entityLocalId) }

        return if (outcome is OutboxCallOutcome.Ok) confirm(entry, outcome.body) else original
    }

    private suspend fun update(entry: OutboxEntryEntity): OutboxSendResult {
        val serverId = entry.targetServerId ?: return OutboxSendResult.Terminal("нет serverId")
        val request = gson.fromJson(entry.payload, UpdateAccountRequestDto::class.java)
        val outcome = outboxCall(entry.operation) { remoteDataSource.update(serverId, request) }

        return when (outcome) {
            is OutboxCallOutcome.Ok -> confirm(entry, dto = null)
            is OutboxCallOutcome.Rejected -> outcome.result
        }
    }

    private suspend fun delete(entry: OutboxEntryEntity): OutboxSendResult {
        val serverId = entry.targetServerId ?: return finishLocalDelete(entry)
        val outcome = outboxCall(entry.operation) { remoteDataSource.delete(serverId) }

        return when (outcome) {
            is OutboxCallOutcome.Ok -> finishLocalDelete(entry)

            // 404 уже трактован как успех: счёта на сервере нет — цель удаления достигнута
            is OutboxCallOutcome.Rejected -> when (outcome.result) {
                is OutboxSendResult.Success -> finishLocalDelete(entry)
                else -> outcome.result
            }
        }
    }

    private suspend fun confirm(entry: OutboxEntryEntity, dto: AccountDto?): OutboxSendResult {
        val local = localDataSource.getByLocalId(entry.entityLocalId)
            ?: return OutboxSendResult.Success

        localDataSource.update(
            local.copy(
                serverId = dto?.id ?: local.serverId,
                updatedAt = dto?.updatedAt ?: local.updatedAt,
                syncStatus = SyncStatus.SYNCED
            )
        )
        return OutboxSendResult.Success
    }

    /**
     * Архивный счёт остаётся локально (помечается синхронизированным), обычный — удаляется.
     */
    private suspend fun finishLocalDelete(entry: OutboxEntryEntity): OutboxSendResult {
        val local = localDataSource.getByLocalId(entry.entityLocalId)
            ?: return OutboxSendResult.Success

        if (local.status == AccountStatus.Deleted.name) {
            localDataSource.update(local.copy(syncStatus = SyncStatus.SYNCED))
        } else {
            localDataSource.delete(local.localId)
        }
        return OutboxSendResult.Success
    }
}
