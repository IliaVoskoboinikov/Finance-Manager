package soft.divan.financemanager.core.data.mapper

import soft.divan.financemanager.core.data.dto.CategoryDto
import soft.divan.financemanager.core.data.entity.CategoryEntity
import soft.divan.financemanager.core.domain.model.Category


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
