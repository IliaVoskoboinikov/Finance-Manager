package soft.divan.financemanager.feature.transactionstoday.impl.presenter.mapper

import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.feature.transactionstoday.impl.presenter.model.CategoryUi

fun Category.toUi(): CategoryUi {
    return CategoryUi(
        name = name,
        emoji = emoji,
    )
}
