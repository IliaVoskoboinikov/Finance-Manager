package soft.divan.financemanager.feature.expenses_income_shared.presenter.mapper

import soft.divan.financemanager.core.domain.model.CurrencyCode
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.model.symbol
import java.math.BigDecimal

fun Transaction.toUi(currency: CurrencyCode): soft.divan.financemanager.feature.expenses_income_shared.presenter.model.UiTransaction {
    return _root_ide_package_.soft.divan.financemanager.feature.expenses_income_shared.presenter.model.UiTransaction(
        id = this.id,
        accountId = this.accountId,
        category = this.category.toUi(),
        amount = this.amount,
        amountFormatted = this.amount.formatWith(currency),
        transactionDate = this.transactionDate,
        comment = this.comment.toString(),
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

fun BigDecimal.formatWith(currency: CurrencyCode): String =
    "${this.stripTrailingZeros().toPlainString()} ${currency.symbol()}"
