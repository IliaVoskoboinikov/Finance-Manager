package soft.divan.financemanager.feature.expenses_income_shared.presenter.mapper

import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.feature.expenses_income_shared.presenter.model.UiCategory


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