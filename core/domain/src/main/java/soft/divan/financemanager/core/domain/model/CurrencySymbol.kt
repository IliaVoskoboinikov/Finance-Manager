package soft.divan.financemanager.core.domain.model

enum class CurrencySymbol(val id: String, val code: String, val symbol: String) {
    RUB("00ea073e-6c01-4f21-8c82-d8933e04a5f1", "RUB", "₽"),
    USD("63f89de5-3eaa-4b14-beed-82590b781db3", "USD", "$"),
    EUR("7626b80a-b3c6-409b-be72-909635095603", "EUR", "€");

    companion object {
        fun fromId(id: String): String {
            return entries.find { it.id == id }?.symbol ?: id
        }

        fun getById(id: String): CurrencySymbol? {
            return entries.find { it.id == id }
        }

        fun fromCode(code: String): String {
            return entries.find { it.code.equals(code, ignoreCase = true) }?.symbol ?: code
        }

        fun fromSymbol(symbol: String): String {
            return entries.find { it.symbol == symbol }?.id ?: symbol
        }
    }
}
