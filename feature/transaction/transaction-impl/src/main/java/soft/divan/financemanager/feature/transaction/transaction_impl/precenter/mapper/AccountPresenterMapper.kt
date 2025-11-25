package soft.divan.financemanager.feature.transaction.transaction_impl.precenter.mapper

import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.model.AccountUi

fun Account.toUi(): AccountUi {
    return AccountUi(
        id = id,
        name = name,
        balance = balance.toString(),
        currency = currency
    )
}