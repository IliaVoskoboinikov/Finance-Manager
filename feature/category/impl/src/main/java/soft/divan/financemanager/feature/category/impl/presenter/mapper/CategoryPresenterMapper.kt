package soft.divan.financemanager.feature.category.impl.presenter.mapper

import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.feature.category.impl.presenter.model.UiCategory

fun Category.toUi(): UiCategory = UiCategory(
    id = id,
    name = name,
    emoji = emoji,
    isIncome = isIncome
)

fun UiCategory.toDomain(): Category = Category(
    id = id,
    name = name,
    emoji = emoji,
    isIncome = isIncome
)
