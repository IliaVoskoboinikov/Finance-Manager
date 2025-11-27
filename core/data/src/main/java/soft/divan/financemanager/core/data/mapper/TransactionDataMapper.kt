package soft.divan.financemanager.core.data.mapper

import soft.divan.financemanager.core.data.dto.TransactionDto
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.finansemanager.core.database.entity.TransactionEntity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


fun TransactionDto.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    accountId = account.id,
    categoryId = category.id,
    amount = amount,
    transactionDate = transactionDate,
    comment = comment,
    createdAt = createdAt,
    updatedAt = updatedAt,
    currencyCode = account.currency,
    isSynced = false,
)

private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = id,
    accountId = accountId,
    categoryId = categoryId,
    amount = amount.toBigDecimal(),
    transactionDate = LocalDateTime.parse(transactionDate, formatter),
    comment = comment,
    createdAt = LocalDateTime.parse(createdAt, formatter),
    updatedAt = LocalDateTime.parse(updatedAt, formatter),
    currencyCode = currencyCode,
)
