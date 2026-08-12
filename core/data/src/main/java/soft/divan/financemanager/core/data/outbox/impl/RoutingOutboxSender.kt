package soft.divan.financemanager.core.data.outbox.impl

import soft.divan.financemanager.core.data.outbox.OutboxSendResult
import soft.divan.financemanager.core.data.outbox.OutboxSender
import soft.divan.financemanager.core.database.entity.OutboxEntryEntity
import soft.divan.financemanager.core.database.model.OutboxEntityType
import javax.inject.Inject

/**
 * Направляет запись очереди отправителю её типа.
 *
 * Благодаря этому [OutboxProcessor] работает с одной зависимостью и ничего не знает ни о типах
 * сущностей, ни об эндпоинтах: добавление нового типа сводится к новому отправителю и ветке здесь.
 */
class RoutingOutboxSender @Inject constructor(
    private val transactionSender: TransactionOutboxSender,
    private val accountSender: AccountOutboxSender
) : OutboxSender {

    override suspend fun send(entry: OutboxEntryEntity): OutboxSendResult =
        when (entry.entityType) {
            OutboxEntityType.TRANSACTION -> transactionSender.send(entry)
            OutboxEntityType.ACCOUNT -> accountSender.send(entry)
        }
}
