package soft.divan.financemanager.core.data.mapper

import soft.divan.financemanager.core.data.dto.CategoryDto
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.finansemanager.core.database.entity.CategoryEntity


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
