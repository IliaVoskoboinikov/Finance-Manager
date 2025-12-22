package soft.divan.financemanager.core.data.mapper

import soft.divan.financemanager.core.data.dto.TransactionDto
import soft.divan.financemanager.core.data.dto.TransactionRequestDto
import soft.divan.financemanager.core.domain.data.DateHelper.dataTimeForApi
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.finansemanager.core.database.entity.TransactionEntity
import java.time.LocalDateTime
import java.time.ZoneOffset


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


fun Transaction.toDto(accountIdServer: Int): TransactionRequestDto = TransactionRequestDto(
    accountId = accountIdServer,
    categoryId = categoryId,
    amount = amount.toString(),
    transactionDate = dataTimeForApi(transactionDate),
    comment = comment
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = idServer,
    accountIdLocal = accountId,
    accountIdServer = null,
    categoryId = categoryId,
    amount = amount.toPlainString(),
    //todo время унифицировать
    transactionDate = transactionDate.atOffset(ZoneOffset.UTC).format(formatter),
    comment = comment.orEmpty(),
    createdAt = createdAt.atOffset(ZoneOffset.UTC).format(formatter),
    updatedAt = updatedAt.atOffset(ZoneOffset.UTC).format(formatter),
    isSynced = true,
    currencyCode = currencyCode,
)
