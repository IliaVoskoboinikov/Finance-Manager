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
    id = this.id,
    accountId = this.accountId,
    categoryId = this.categoryId,
    amount = this.amount.toBigDecimal(),
    transactionDate = LocalDateTime.parse(this.transactionDate, formatter),
    comment = this.comment,
    createdAt = LocalDateTime.parse(this.createdAt, formatter),
    updatedAt = LocalDateTime.parse(this.updatedAt, formatter),
    currencyCode = currencyCode,
)


fun Transaction.toDto(): TransactionRequestDto = TransactionRequestDto(
    accountId = this.accountId,
    categoryId = this.categoryId,
    amount = this.amount.toString(),
    transactionDate = dataTimeForApi(transactionDate),
    comment = this.comment
)

fun Transaction.toEntity(): TransactionEntity = TransactionEntity(
    id = this.id,
    accountId = this.accountId,
    categoryId = this.categoryId,
    amount = this.amount.toPlainString(),
    //todo время унифицировать
    transactionDate = this.transactionDate.atOffset(ZoneOffset.UTC).format(formatter),
    comment = this.comment.orEmpty(),
    createdAt = this.createdAt.atOffset(ZoneOffset.UTC).format(formatter),
    updatedAt = this.updatedAt.atOffset(ZoneOffset.UTC).format(formatter),
    isSynced = true,
    currencyCode = currencyCode
)
