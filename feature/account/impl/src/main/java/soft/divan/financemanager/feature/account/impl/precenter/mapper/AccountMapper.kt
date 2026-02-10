package soft.divan.financemanager.feature.account.impl.precenter.mapper

import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.utli.UiDateFormatter
import soft.divan.financemanager.feature.account.impl.precenter.model.AccountUi

fun AccountUi.toDomain(): Account {
    return Account(
        id = id,
        name = name,
        balance = balance.toBigDecimal(),
        currency = currency,
        createdAt = UiDateFormatter.parse(createdAt),
        updatedAt = UiDateFormatter.parse(updatedAt)
    )
}

fun Account.toUi(): AccountUi {
    return AccountUi(
        id = id,
        name = name,
        balance = balance.stripTrailingZeros().toPlainString(),
        currency = currency,
        createdAt = UiDateFormatter.formatDateTime(createdAt),
        updatedAt = UiDateFormatter.formatDateTime(updatedAt)
    )
}
// Revue me>>
