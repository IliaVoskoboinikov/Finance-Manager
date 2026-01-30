package soft.divan.financemanager.core.data.mapper

import soft.divan.financemanager.core.data.dto.CategoryDto
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.finansemanager.core.database.entity.CategoryEntity

fun CategoryDto.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    name = name,
    emoji = emoji,
    isIncome = isIncome
)

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    emoji = emoji,
    isIncome = isIncome
)
