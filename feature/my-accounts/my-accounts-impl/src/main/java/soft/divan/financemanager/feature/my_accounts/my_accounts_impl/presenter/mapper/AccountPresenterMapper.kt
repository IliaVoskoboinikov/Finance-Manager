package soft.divan.financemanager.feature.my_accounts.my_accounts_impl.presenter.mapper


import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.model.CurrencySymbol
import soft.divan.financemanager.feature.my_accounts.my_accounts_impl.presenter.model.AccountUiModel

fun Account.toUiModel(): AccountUiModel {
    val currencySymbol = CurrencySymbol.fromCode(currency)
    return AccountUiModel(
        id = id,
        name = name,
        balance = "${balance.toPlainString()} $currencySymbol",
        currency = currencySymbol,
    )
}


