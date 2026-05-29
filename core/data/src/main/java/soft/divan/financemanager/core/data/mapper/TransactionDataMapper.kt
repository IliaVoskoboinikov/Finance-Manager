package soft.divan.financemanager.core.data.mapper

import soft.divan.financemanager.core.data.dto.TransactionDto
import soft.divan.financemanager.core.data.dto.TransactionRequestDto
import soft.divan.financemanager.core.data.dto.UpdateTransactionRequestDto
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.database.entity.TransactionEntity
import soft.divan.financemanager.core.database.model.SyncStatus
import soft.divan.financemanager.core.domain.model.TransactionType

fun TransactionDto.toEntity(
    localId: String,
    accountLocalId: String,
    currencyCode: String,
    type: TransactionType,
    syncStatus: SyncStatus
): TransactionEntity = TransactionEntity(
    localId = localId,
    serverId = id,
    accountLocalId = accountLocalId,
    accountServerId = accountId,
    categoryId = categoryId,
    currencyCode = currencyCode,
    amount = amount.toString(),
    transactionDate = dateTime,
    comment = comment.orEmpty(),
    createdAt = createdAt,
    updatedAt = updatedAt,
    syncStatus = syncStatus,
    type = type.name,
    targetAccountLocalId = null
)

fun TransactionEntity.toUpdateDto(accountServerId: String): UpdateTransactionRequestDto = UpdateTransactionRequestDto(
    accountId = accountServerId,
    categoryId = categoryId,
    amount = amount.toDouble(),
    dateTime = transactionDate,
    comment = comment
)

fun TransactionEntity.toDto(accountServerId: String): TransactionRequestDto = TransactionRequestDto(
    id = serverId,
    accountId = accountServerId,
    categoryId = categoryId,
    amount = amount.toDouble(),
    dateTime = transactionDate,
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
    serverId: String?,
    accountServerId: String?,
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
