package soft.divan.financemanager.presenter.mapper

import soft.divan.financemanager.domain.model.Category
import soft.divan.financemanager.presenter.ui.model.UiCategory

fun Category.toUi(): UiCategory {
    return UiCategory(
        id = this.id,
        name = this.name,
        emoji = this.emoji,
        isIncome = this.isIncome
    )
}


fun UiCategory.toDomain(): Category {
    return Category(
        id = this.id,
        name = this.name,
        emoji = this.emoji,
        isIncome = this.isIncome
    )
}