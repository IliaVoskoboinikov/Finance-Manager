package soft.divan.financemanager.presenter.mapper

import soft.divan.financemanager.category.presenter.mapper.toUi
import soft.divan.financemanager.domain.model.CurrencyCode
import soft.divan.financemanager.domain.model.Transaction
import soft.divan.financemanager.domain.model.symbol
import soft.divan.financemanager.presenter.ui.model.UiTransaction
import java.math.BigDecimal

fun Transaction.toUi(currency: CurrencyCode): UiTransaction {
    return UiTransaction(
        id = this.id,
        accountId = this.accountId,
        category = this.category.toUi(),
        amount = this.amount,
        amountFormatted = this.amount.formatWith(currency),
        transactionDate = this.transactionDate,
        comment = this.comment,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

fun BigDecimal.formatWith(currency: CurrencyCode): String =
    "${this.stripTrailingZeros().toPlainString()} ${currency.symbol()}"
