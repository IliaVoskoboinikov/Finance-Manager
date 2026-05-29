package soft.divan.financemanager.feature.category.impl.presenter.mapper

import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.feature.category.impl.presenter.model.UiCategory
import java.time.Instant

fun Category.toUi(): UiCategory = UiCategory(
    id = id,
    name = name,
    emoji = emoji,
    isIncome = isIncome
)

fun UiCategory.toDomain(): Category = Category(
    id = id,
    createdAt = Instant.now(),
    updatedAt = Instant.now(),
    name = name,
    emoji = emoji,
    isIncome = isIncome
)
