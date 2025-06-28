package soft.divan.financemanager.presenter.mapper

import soft.divan.financemanager.domain.model.Account
import soft.divan.financemanager.presenter.ui.model.AccountUiModel

fun Account.toUiModel(): AccountUiModel {
    val currencySymbol = CurrencySymbol.fromCode(currency)
    return AccountUiModel(
        id = this.id,
        name = this.name,
        balance = "${balance.toPlainString()} $currencySymbol",
        currency = currencySymbol
    )
}

enum class CurrencySymbol(val code: String, val symbol: String) {
    USD("USD", "$"),
    EUR("EUR", "€"),
    RUB("RUB", "₽");

    companion object {
        fun fromCode(code: String): String {
            return values().find { it.code.equals(code, ignoreCase = true) }?.symbol ?: code
        }
    }
}
