package soft.divan.financemanager.presenter.mapper

import jakarta.inject.Inject
import soft.divan.financemanager.domain.model.Account
import soft.divan.financemanager.presenter.ui.model.AccountItem
import soft.divan.financemanager.presenter.ui.model.AccountUiState
import java.math.BigDecimal


class AccountUiStateMapper @Inject constructor() {

    fun mapToUiState(accounts: List<Account>): AccountUiState {
        if (accounts.isEmpty()) {
            return AccountUiState.Success(emptyList())
        }

        // Суммируем баланс
        val totalBalance = accounts.map { it.balance }.reduce { acc, next -> acc + next }
        val currency = accounts.first().currency // Предполагаем, что все в одной валюте

        val items = listOf(
            AccountItem.Balance(
                emoji = "💰",
                label = "Все счета",
                amount = formatAmount(totalBalance, currency)
            ),
            AccountItem.Currency(
                label = "Валюта",
                symbol = currencySymbol(currency)
            )
        )

        return AccountUiState.Success(items)
    }

    private fun formatAmount(amount: BigDecimal, currency: String): String {
        val symbol = currencySymbol(currency)
        val formatted = "%,.2f".format(amount)
        return "$formatted $symbol"
    }

    private fun currencySymbol(currency: String): String {
        return when (currency) {
            "RUB" -> "₽"
            "USD" -> "$"
            "EUR" -> "€"
            "GBP" -> "£"
            else -> currency
        }
    }
}
