package soft.divan.financemanager.feature.account.impl.precenter.mapper

import soft.divan.financemanager.core.domain.data.DateHelper
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.feature.account.impl.precenter.model.AccountUi

fun AccountUi.toDomain(): Account {
    return Account(
        id = id,
        name = name,
        balance = balance.toBigDecimal(),
        currency = currency,
        createdAt = DateHelper.parseDisplayDateTime(createdAt),
        updatedAt = DateHelper.parseDisplayDateTime(createdAt),
    )
}

fun Account.toUi(): AccountUi {
    return AccountUi(
        id = id,
        name = name,
        balance = balance.stripTrailingZeros().toPlainString(),
        currency = currency,
        createdAt = DateHelper.formatTimeForDisplay(createdAt),
        updatedAt = DateHelper.formatTimeForDisplay(updatedAt),
    )
}