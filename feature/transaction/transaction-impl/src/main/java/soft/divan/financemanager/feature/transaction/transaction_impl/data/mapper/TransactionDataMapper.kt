package soft.divan.financemanager.feature.transaction.transaction_impl.data.mapper

import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.util.DateHelper.dataTimeForApi
import soft.divan.financemanager.feature.transaction.transaction_impl.data.dto.TransactionRequestDto
import soft.divan.finansemanager.core.database.entity.TransactionEntity
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


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


fun Transaction.toDto(): TransactionRequestDto = TransactionRequestDto(
    accountId = accountId,
    categoryId = categoryId,
    amount = amount.toString(),
    transactionDate = dataTimeForApi(transactionDate),
    comment = comment
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = id,
    accountId = accountId,
    categoryId = categoryId,
    amount = amount.toPlainString(),
    //todo время унифицировать
    transactionDate = transactionDate.atOffset(ZoneOffset.UTC).format(formatter),
    comment = comment.orEmpty(),
    createdAt = createdAt.atOffset(ZoneOffset.UTC).format(formatter),
    updatedAt = updatedAt.atOffset(ZoneOffset.UTC).format(formatter),
    isSynced = true,
    currencyCode = currencyCode
)
