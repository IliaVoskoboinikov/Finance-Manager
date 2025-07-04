package soft.divan.financemanager.presenter.mapper

import soft.divan.financemanager.domain.model.Account
import soft.divan.financemanager.domain.model.AccountBrief
import soft.divan.financemanager.presenter.ui.model.AccountUiModel
import java.math.BigDecimal

fun Account.toUiModel(): AccountUiModel {
    val currencySymbol = CurrencySymbol.fromCode(currency)
    return AccountUiModel(
        id = this.id,
        name = this.name,
        balance = "${balance.toPlainString()} $currencySymbol",
        currency = currencySymbol,
    )
}

fun AccountBrief.toUiModel(): AccountUiModel {
    val currencySymbol = CurrencySymbol.fromCode(currency)
    return AccountUiModel(
        id = this.id,
        name = this.name,
        balance = "${balance.toPlainString()} $currencySymbol",
        currency = currencySymbol,
    )
}

fun AccountUiModel.toDomain(): AccountBrief {
    return AccountBrief(
        id = this.id,
        name = this.name,
        balance = this.balance.toBigDecimalOrZero(),
        currency = CurrencySymbol.fromSymbol(this.currency),
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
        fun fromSymbol(symbol: String): String {
            return values().find { it.symbol == symbol }?.code ?: symbol
        }
    }
}

fun String.toBigDecimalOrZero(): BigDecimal {
    return try {
        this.filter { it.isDigit() || it == '.' || it == ',' }
            .replace(",", ".")
            .toBigDecimal()
    } catch (e: Exception) {
        BigDecimal.ZERO
    }
}
