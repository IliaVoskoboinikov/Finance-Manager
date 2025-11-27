package soft.divan.financemanager.feature.transaction.transaction_impl.precenter.mapper

import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.model.UiCategory


fun Category.toUi(): UiCategory {
    return UiCategory(
        id = id,
        name = name,
        emoji = emoji,
        isIncome = isIncome
    )
}
//todo

