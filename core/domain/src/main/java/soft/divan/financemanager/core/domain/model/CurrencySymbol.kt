package soft.divan.financemanager.core.domain.model

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
// Revue me>>
