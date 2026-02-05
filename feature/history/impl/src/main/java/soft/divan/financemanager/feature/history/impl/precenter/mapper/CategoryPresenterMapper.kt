package soft.divan.financemanager.feature.history.impl.precenter.mapper

import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.feature.history.impl.precenter.model.UiCategory

fun Category.toUi(): UiCategory {
    return UiCategory(
        name = name,
        emoji = emoji
    )
}
