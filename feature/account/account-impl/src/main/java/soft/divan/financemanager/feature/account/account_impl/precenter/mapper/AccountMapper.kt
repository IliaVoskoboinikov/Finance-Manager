package soft.divan.financemanager.feature.account.account_impl.precenter.mapper

import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.model.AccountBrief
import soft.divan.financemanager.feature.account.account_impl.precenter.model.AccountUiModel

fun AccountUiModel.toDomain(): AccountBrief {
    return AccountBrief(
        id = id ?: -1,
        name = name,
        balance = balance.toBigDecimal(),
        currency = currency,
    )
}

fun Account.toUi(): AccountUiModel {
    return AccountUiModel(
        id = id,
        name = name,
        balance = balance.toString(),
        currency = currency,
    )
}