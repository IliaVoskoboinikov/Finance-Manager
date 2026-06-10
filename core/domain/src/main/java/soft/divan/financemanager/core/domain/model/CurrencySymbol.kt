package soft.divan.financemanager.core.domain.model

enum class CurrencySymbol(val id: String, val code: String, val symbol: String) {
    RUB("d67bdffe-9f2d-45e4-809a-c566f537dfb7", "RUB", "₽"),
    USD("0fbe774f-42fd-4814-b3c2-6ea659b4e595", "USD", "$"),
    EUR("e17d279c-4ac8-4679-894d-5b5f5caed741", "EUR", "€");

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
