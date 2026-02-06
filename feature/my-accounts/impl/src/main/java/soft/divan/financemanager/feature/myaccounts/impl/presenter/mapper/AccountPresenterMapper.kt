package soft.divan.financemanager.feature.myaccounts.impl.presenter.mapper

import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.model.CurrencySymbol
import soft.divan.financemanager.feature.myaccounts.impl.presenter.model.MyAccountsUiModel

fun Account.toUiModel(): MyAccountsUiModel {
    val currencySymbol = CurrencySymbol.fromCode(currency)
    return MyAccountsUiModel(
        id = id,
        name = name,
        balance = "${balance.toPlainString()} $currencySymbol",
        currency = currencySymbol
    )
}
// Revue me>>
