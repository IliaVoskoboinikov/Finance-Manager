package soft.divan.financemanager.core.data.mapper

import soft.divan.financemanager.core.data.dto.TransactionDto
import soft.divan.financemanager.core.data.dto.TransactionRequestDto
import soft.divan.financemanager.core.data.dto.TransactionResponseCreateDto
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.database.entity.TransactionEntity
import soft.divan.financemanager.core.database.model.SyncStatus
import soft.divan.financemanager.core.domain.model.TransactionType
// todo
/*fun TransactionDto.toEntity(
    localId: String,
    accountLocalId: String,
    syncStatus: SyncStatus
): TransactionEntity = TransactionEntity(
    localId = localId,
    serverId = id,
    accountLocalId = accountLocalId,
    accountServerId = account.id,
    categoryId = category.id,
    currencyCode = account.currency,
    amount = amount,
    transactionDate = transactionDate,
    comment = comment,
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus,
    type = TODO(),
    targetAccountLocalId = TODO()
)*/

fun TransactionEntity.toDto(accountServerId: Int): TransactionRequestDto = TransactionRequestDto(
    accountId = accountServerId,
    categoryId = categoryId,
    amount = amount,
    transactionDate = transactionDate,
    comment = comment
)

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = localId,
    accountLocalId = accountLocalId,
    targetAccountLocalId = targetAccountLocalId,
    currencyCode = currencyCode,
    categoryId = categoryId,
    amount = amount.toBigDecimal(),
    type = TransactionType.valueOf(type),
    transactionDate = TimeMapper.fromApi(transactionDate),
    comment = comment,
    createdAt = TimeMapper.fromApi(createdAt),
    updatedAt = TimeMapper.fromApi(updatedAt)
)

fun Transaction.toEntity(
    serverId: Int?,
    accountServerId: Int?,
    syncStatus: SyncStatus
): TransactionEntity = TransactionEntity(
    localId = id,
    serverId = serverId,
    accountLocalId = accountLocalId,
    targetAccountLocalId = targetAccountLocalId,
    accountServerId = accountServerId,
    type = type.name,
    categoryId = categoryId,
    currencyCode = currencyCode,
    amount = amount.toPlainString(),
    transactionDate = TimeMapper.toApi(transactionDate),
    comment = comment.orEmpty(),
    createdAt = TimeMapper.toApi(createdAt),
    updatedAt = TimeMapper.toApi(updatedAt),
    syncStatus = syncStatus
)
// todo
/*
fun TransactionResponseCreateDto.toEntity(
    localId: String,
    accountLocalId: String,
    currencyCode: String,
    syncStatus: SyncStatus
): TransactionEntity = TransactionEntity(
    localId = localId,
    serverId = id,
    accountLocalId = accountLocalId,
    accountServerId = accountId,
    categoryId = categoryId,
    currencyCode = currencyCode,
    amount = amount,
    transactionDate = transactionDate,
    comment = comment.orEmpty(),
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus
)
*/
