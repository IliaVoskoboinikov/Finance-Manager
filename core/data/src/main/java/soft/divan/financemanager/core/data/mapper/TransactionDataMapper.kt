package soft.divan.financemanager.core.data.mapper

import soft.divan.financemanager.core.data.dto.TransactionDto
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.finansemanager.core.database.entity.TransactionEntity
import java.time.LocalDateTime


fun TransactionDto.toEntity(accountIdLocal: String): TransactionEntity = TransactionEntity(
    id = id,
    accountIdLocal = accountIdLocal,
    accountIdServer = account.id,
    categoryId = category.id,
    amount = amount,
    transactionDate = transactionDate,
    comment = comment,
    createdAt = createdAt,
    updatedAt = updatedAt,
    currencyCode = account.currency,
    isSynced = false,
)


fun TransactionEntity.toDomain(): Transaction = Transaction(
    idServer = id,
    accountId = accountIdLocal,
    categoryId = categoryId,
    amount = amount.toBigDecimal(),
    transactionDate = LocalDateTime.parse(transactionDate, formatter),
    comment = comment,
    createdAt = LocalDateTime.parse(createdAt, formatter),
    updatedAt = LocalDateTime.parse(updatedAt, formatter),
    currencyCode = currencyCode,
)
