package soft.divan.financemanager.feature.category.category_impl.presenter.mapper

import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.feature.category.category_impl.presenter.model.UiCategory


fun Category.toUi(): UiCategory {
    return UiCategory(
        id = this.id,
        name = this.name,
        emoji = this.emoji,
        isIncome = this.isIncome
    )
}
//todo

fun UiCategory.toDomain(): Category {
    return Category(
        id = this.id,
        name = this.name,
        emoji = this.emoji,
        isIncome = this.isIncome
    )
}