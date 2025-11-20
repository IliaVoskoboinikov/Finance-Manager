package soft.divan.financemanager.core.domain.model

import java.math.BigDecimal

enum class CurrencySymbol(val code: String, val symbol: String) {
    USD("USD", "$"),
    EUR("EUR", "€"),
    RUB("RUB", "₽");

    companion object {
        fun fromCode(code: String): String {
            return entries.find { it.code.equals(code, ignoreCase = true) }?.symbol ?: code
        }

        fun fromSymbol(symbol: String): String {
            return entries.find { it.symbol == symbol }?.code ?: symbol
        }
    }
}

@JvmInline
value class CurrencyCode(val code: String) {
    init {
        require(code in CurrencySymbol.entries.map { it.code }) { "Unsupported currency code" }
    }

    override fun toString(): String = code
}

fun CurrencyCode.symbol(): String =
    CurrencySymbol.fromCode(this.code)

fun BigDecimal.formatWith(currency: CurrencyCode): String =
    "${this.stripTrailingZeros().toPlainString()} ${currency.symbol()}"