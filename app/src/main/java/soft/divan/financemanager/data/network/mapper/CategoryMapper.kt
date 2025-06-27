package soft.divan.financemanager.data.network.mapper

import soft.divan.financemanager.data.network.dto.CategoryDto
import soft.divan.financemanager.data.network.dto.TransactionDto
import soft.divan.financemanager.data.network.entity.AccountEntity
import soft.divan.financemanager.data.network.entity.CategoryEntity
import soft.divan.financemanager.data.network.entity.TransactionEntity
import soft.divan.financemanager.domain.model.Account
import soft.divan.financemanager.domain.model.Category
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


fun CategoryDto.toEntity(): CategoryEntity = CategoryEntity(
    id = this.id,
    name = this.name,
    emoji = this.emoji,
    isIncome = this.isIncome
)

fun CategoryEntity.toDomain(): Category = Category(
    id = this.id,
    name = this.name,
    emoji = this.emoji,
    isIncome = this.isIncome
)
