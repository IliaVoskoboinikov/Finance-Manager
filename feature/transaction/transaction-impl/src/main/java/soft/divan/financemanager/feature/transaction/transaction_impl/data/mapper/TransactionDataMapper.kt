package soft.divan.financemanager.feature.transaction.transaction_impl.data.mapper

import soft.divan.financemanager.core.domain.data.DateHelper.dataTimeForApi
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.feature.transaction.transaction_impl.data.dto.TransactionRequestDto
import soft.divan.finansemanager.core.database.entity.TransactionEntity
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

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
