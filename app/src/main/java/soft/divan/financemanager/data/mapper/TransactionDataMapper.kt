package soft.divan.financemanager.data.mapper

import soft.divan.financemanager.data.entity.TransactionEntity
import soft.divan.financemanager.data.network.dto.TransactionDto
import soft.divan.financemanager.domain.model.Transaction
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


fun TransactionDto.toEntity(): TransactionEntity = TransactionEntity(
    id = this.id,
    account = this.account.toEntity(),
    category = this.category.toEntity(),
    amount = this.amount,
    transactionDate = this.transactionDate,
    comment = this.comment,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)

private val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

fun TransactionEntity.toDomain(): Transaction = Transaction(
    id = this.id,
    accountId = this.account.id,
    category = this.category.toDomain(),
    amount = this.amount.toBigDecimal(),
    transactionDate = LocalDateTime.parse(this.transactionDate, formatter),
    comment = this.comment,
    createdAt = LocalDateTime.parse(this.createdAt, formatter),
    updatedAt = LocalDateTime.parse(this.updatedAt, formatter)
)
