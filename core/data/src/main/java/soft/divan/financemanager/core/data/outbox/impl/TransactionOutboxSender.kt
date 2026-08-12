package soft.divan.financemanager.core.data.outbox.impl

import com.google.gson.Gson
import soft.divan.financemanager.core.data.dto.TransactionDto
import soft.divan.financemanager.core.data.dto.TransactionRequestDto
import soft.divan.financemanager.core.data.dto.UpdateTransactionRequestDto
import soft.divan.financemanager.core.data.outbox.OutboxCallOutcome
import soft.divan.financemanager.core.data.outbox.OutboxSendResult
import soft.divan.financemanager.core.data.outbox.OutboxSender
import soft.divan.financemanager.core.data.outbox.outboxCall
import soft.divan.financemanager.core.data.source.TransactionLocalDataSource
import soft.divan.financemanager.core.data.source.TransactionRemoteDataSource
import soft.divan.financemanager.core.database.entity.OutboxEntryEntity
import soft.divan.financemanager.core.database.model.OutboxOperation
import soft.divan.financemanager.core.database.model.SyncStatus
import javax.inject.Inject

/**
 * Отправляет операции над транзакциями из очереди и отражает результат в локальной БД.
 *
 * Тело запроса берётся из снимка ([OutboxEntryEntity.payload]) — таким, каким было в момент
 * операции. Локальная строка читается только чтобы **применить ответ** (проставить `serverId`,
 * снять pending-статус), но не чтобы собрать запрос заново.
 */
class TransactionOutboxSender @Inject constructor(
    private val remoteDataSource: TransactionRemoteDataSource,
    private val localDataSource: TransactionLocalDataSource,
    private val gson: Gson
) : OutboxSender {

    override suspend fun send(entry: OutboxEntryEntity): OutboxSendResult =
        when (entry.operation) {
            OutboxOperation.CREATE -> create(entry)
            OutboxOperation.UPDATE -> update(entry)
            OutboxOperation.DELETE -> delete(entry)
        }

    /**
     * Создание с подстраховкой на случай потери ACK.
     *
     * Сервер мог применить `POST` и не доставить ответ; тогда повтор с тем же клиентским id вернёт
     * ошибку «уже существует», которую по коду не отличить от настоящего сбоя. Поэтому неуспех
     * перепроверяется чтением: если транзакция на сервере есть — операция удалась.
     * Подробнее: [docs/idempotency.md](../../../../../../../../../../docs/idempotency.md).
     */
    private suspend fun create(entry: OutboxEntryEntity): OutboxSendResult {
        val request = gson.fromJson(entry.payload, TransactionRequestDto::class.java)
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

    /** Read-back: если запись уже на сервере, значит предыдущая попытка всё-таки долетела. */
    private suspend fun readBack(
        entry: OutboxEntryEntity,
        original: OutboxSendResult
    ): OutboxSendResult {
        val outcome = outboxCall(entry.operation) { remoteDataSource.get(entry.entityLocalId) }

        return if (outcome is OutboxCallOutcome.Ok) confirm(entry, outcome.body) else original
    }

    private suspend fun update(entry: OutboxEntryEntity): OutboxSendResult {
        val serverId = entry.targetServerId ?: return OutboxSendResult.Terminal("нет serverId")
        val request = gson.fromJson(entry.payload, UpdateTransactionRequestDto::class.java)
        val outcome = outboxCall(entry.operation) { remoteDataSource.update(serverId, request) }

        return when (outcome) {
            is OutboxCallOutcome.Ok -> confirm(entry, dto = null)
            is OutboxCallOutcome.Rejected -> outcome.result
        }
    }

    private suspend fun delete(entry: OutboxEntryEntity): OutboxSendResult {
        // Записи на сервере не было — достаточно локального удаления
        val serverId = entry.targetServerId ?: return removeLocally(entry)
        val outcome = outboxCall(entry.operation) { remoteDataSource.delete(serverId) }

        return when (outcome) {
            is OutboxCallOutcome.Ok -> removeLocally(entry)

            // 404 уже трактован как успех: записи на сервере нет — цель удаления достигнута
            is OutboxCallOutcome.Rejected -> when (outcome.result) {
                is OutboxSendResult.Success -> removeLocally(entry)
                else -> outcome.result
            }
        }
    }

    /**
     * Отмечает локальную строку синхронизированной. [dto] непустой только для создания — из него
     * берётся присвоенный сервером идентификатор.
     */
    private suspend fun confirm(entry: OutboxEntryEntity, dto: TransactionDto?): OutboxSendResult {
        val local = localDataSource.getByLocalId(entry.entityLocalId)
            ?: return OutboxSendResult.Success // строки уже нет локально — подтверждать нечего

        localDataSource.update(
            local.copy(
                serverId = dto?.id ?: local.serverId,
                updatedAt = dto?.updatedAt ?: local.updatedAt,
                syncStatus = SyncStatus.SYNCED
            )
        )
        return OutboxSendResult.Success
    }

    private suspend fun removeLocally(entry: OutboxEntryEntity): OutboxSendResult {
        localDataSource.delete(entry.entityLocalId)
        return OutboxSendResult.Success
    }
}
