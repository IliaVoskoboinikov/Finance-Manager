package soft.divan.finansemanager.account.presenter.mapper


import soft.divan.core.currency.CurrencySymbol
import soft.divan.finansemanager.account.domain.model.Account
import soft.divan.finansemanager.account.domain.model.AccountBrief
import soft.divan.finansemanager.account.presenter.model.AccountUiModel
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


fun String.toBigDecimalOrZero(): BigDecimal {
    return try {
        this.filter { it.isDigit() || it == '.' || it == ',' }
            .replace(",", ".")
            .toBigDecimal()
    } catch (e: Exception) {
        BigDecimal.ZERO
    }
}
