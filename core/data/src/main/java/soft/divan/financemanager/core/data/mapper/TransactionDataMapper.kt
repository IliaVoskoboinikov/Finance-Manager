package soft.divan.financemanager.core.data.mapper

import soft.divan.financemanager.core.data.dto.TransactionDto
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.finansemanager.core.database.entity.TransactionEntity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


fun TransactionDto.toEntity(): TransactionEntity = TransactionEntity(
    id = this.id,
    accountId = this.account.id,
    categoryId = this.category.id,
    amount = this.amount,
    transactionDate = this.transactionDate,
    comment = this.comment,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt
)

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
)
