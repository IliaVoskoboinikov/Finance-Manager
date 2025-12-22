package soft.divan.financemanager.feature.transaction.transaction_impl.precenter.mapper

import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.model.CategoryUi


fun Category.toUi(): CategoryUi {
    return CategoryUi(
        id = id,
        name = name,
        emoji = emoji,
    )
}

