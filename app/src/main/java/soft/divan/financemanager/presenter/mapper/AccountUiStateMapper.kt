package soft.divan.financemanager.presenter.mapper

import soft.divan.financemanager.domain.model.Account
import soft.divan.financemanager.presenter.ui.model.AccountUiItem
import soft.divan.financemanager.presenter.ui.model.AccountUiState
import java.math.BigDecimal
import javax.inject.Inject


class AccountUiStateMapper @Inject constructor() {

    fun mapToUiState(account: Account): AccountUiState {

        // Суммируем баланс
        val totalBalance = account.balance
        val currency = account.currency

        val items = listOf(
            AccountUiItem.Balance(
                emoji = "💰",
                label = "Все счета",
                amount = formatAmount(totalBalance, currency)
            ),
            AccountUiItem.Currency(
                label = "Валюта",
                symbol = currencySymbol(currency)
            )
        )

        return AccountUiState.Loading
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
