package soft.divan.financemanager.feature.transactions_today.transactions_today_impl.presenter.mapper

import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.feature.transactions_today.transactions_today_impl.presenter.model.CategoryUi

fun Category.toUi(): CategoryUi {
    return CategoryUi(
        name = name,
        emoji = emoji,
    )
}