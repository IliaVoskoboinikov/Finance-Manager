package soft.divan.financemanager.data.mapper

import soft.divan.financemanager.data.entity.CategoryEntity
import soft.divan.financemanager.data.network.dto.CategoryDto
import soft.divan.financemanager.domain.model.Category


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
