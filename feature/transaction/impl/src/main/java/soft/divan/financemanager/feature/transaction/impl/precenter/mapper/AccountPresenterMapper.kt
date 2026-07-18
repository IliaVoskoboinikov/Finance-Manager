package soft.divan.financemanager.feature.transaction.impl.precenter.mapper

import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.feature.transaction.impl.precenter.model.AccountUi

fun Account.toUi(archived: Boolean = false): AccountUi {
    return AccountUi(
        id = id,
        name = name,
        balance = balance.toString(),
        currencyId = currencyId,
        archived = archived
    )
}
