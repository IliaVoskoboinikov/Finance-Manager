package soft.divan.financemanager.feature.transaction.transaction_impl.data.mapper

import soft.divan.financemanager.core.data.dto.TransactionDto
import soft.divan.financemanager.core.data.entity.TransactionEntity
import soft.divan.financemanager.core.data.mapper.toDomain
import soft.divan.financemanager.core.data.mapper.toEntity
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.util.DateHelper.dataTimeForApi
import soft.divan.financemanager.feature.transaction.transaction_impl.data.dto.TransactionRequestDto
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


fun Transaction.toDto(): TransactionRequestDto = TransactionRequestDto(
    accountId = this.accountId,
    categoryId = this.category.id,
    amount = this.amount.toString(),
    transactionDate = dataTimeForApi(transactionDate),
    comment = this.comment

)