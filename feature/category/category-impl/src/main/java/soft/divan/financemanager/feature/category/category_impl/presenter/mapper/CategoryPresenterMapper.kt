package soft.divan.financemanager.feature.category.category_impl.presenter.mapper

import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.feature.category.category_impl.presenter.model.UiCategory


fun Category.toUi(): UiCategory {
    return UiCategory(
        id = id,
        name = name,
        emoji = emoji,
        isIncome = isIncome
    )
}
//todo

fun UiCategory.toDomain(): Category {
    return Category(
        id = id,
        name = name,
        emoji = emoji,
        isIncome = isIncome
    )
}