package soft.divan.financemanager.core.data.mapper

import soft.divan.financemanager.core.data.dto.TransactionDto
import soft.divan.financemanager.core.data.dto.TransactionRequestDto
import soft.divan.financemanager.core.data.dto.UpdateTransactionRequestDto
import soft.divan.financemanager.core.database.entity.TransactionEntity
import soft.divan.financemanager.core.database.model.SyncStatus
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.model.TransactionType

fun TransactionDto.toEntity(
    localId: String,
    accountLocalId: String,
    currencyId: String,
    type: TransactionType,
    syncStatus: SyncStatus
): TransactionEntity = TransactionEntity(
    localId = localId,
    serverId = id,
    accountLocalId = accountLocalId,
    categoryId = categoryId,
    currencyId = currencyId,
    amount = amount.toPlainString(),
    transactionDate = dateTime,
    comment = comment.orEmpty(),
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus,
    type = type.name,
    targetAccountLocalId = null
)

/** Собирает запрос на правку; [accountId] — id, под которым сервер знает счёт сейчас. */
fun TransactionEntity.toUpdateDto(accountId: String): UpdateTransactionRequestDto =
    UpdateTransactionRequestDto(
        accountId = accountId,
        categoryId = categoryId,
        amount = amount.toBigDecimal(),
        dateTime = transactionDate,
        comment = comment
    )

/**
 * Собирает запрос на создание транзакции (`POST /transaction`).
 *
 * `id = localId` — клиент передаёт свой стабильный UUID как идемпотентный ключ. Сервер
 * принимает клиентский id (upsert-by-id) и на повторную отправку того же id отбивает дубль
 * по первичному ключу. Это защищает от двойного создания транзакции (и, как следствие,
 * двойного пересчёта баланса на бэке) при потере ACK и ретраях.
 *
 * [accountId] — id, под которым сервер знает счёт сейчас: в строке транзакции его нет, он
 * берётся из строки счёта в момент постановки в очередь.
 */
fun TransactionEntity.toDto(accountId: String): TransactionRequestDto = TransactionRequestDto(
    id = localId,
    accountId = accountId,
    categoryId = categoryId,
    amount = amount.toBigDecimal(),
    dateTime = transactionDate,
    comment = comment
)

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = localId,
    accountLocalId = accountLocalId,
    targetAccountLocalId = targetAccountLocalId,
    currencyId = currencyId,
    categoryId = categoryId,
    amount = amount.toBigDecimal(),
    type = TransactionType.valueOf(type),
    transactionDate = TimeMapper.fromApi(transactionDate),
    comment = comment,
    createdAt = TimeMapper.fromApi(createdAt),
    updatedAt = TimeMapper.fromApi(updatedAt)
)

fun Transaction.toEntity(
    serverId: String?,
    syncStatus: SyncStatus
): TransactionEntity = TransactionEntity(
    localId = id,
    serverId = serverId,
    accountLocalId = accountLocalId,
    targetAccountLocalId = targetAccountLocalId,
    type = type.name,
    categoryId = categoryId,
    currencyId = currencyId,
    amount = amount.toPlainString(),
    transactionDate = TimeMapper.toApi(transactionDate),
    comment = comment.orEmpty(),
    createdAt = TimeMapper.toApi(createdAt),
    updatedAt = TimeMapper.toApi(updatedAt),
    syncStatus = syncStatus
)
