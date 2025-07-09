package soft.divan.financemanager.category.data.mapper

import soft.divan.financemanager.category.data.entity.CategoryEntity
import soft.divan.financemanager.category.domain.model.Category
import soft.divan.financemanager.core.network.dto.CategoryDto


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
