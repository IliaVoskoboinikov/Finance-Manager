package soft.divan.financemanager.feature.transactions_today.transactions_today_impl.presenter.mapper

import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.feature.transactions_today.transactions_today_impl.presenter.model.UiCategory

fun Category.toUi(): UiCategory {
    return UiCategory(
        id = id,
        name = name,
        emoji = emoji,
        isIncome = isIncome
    )
}
