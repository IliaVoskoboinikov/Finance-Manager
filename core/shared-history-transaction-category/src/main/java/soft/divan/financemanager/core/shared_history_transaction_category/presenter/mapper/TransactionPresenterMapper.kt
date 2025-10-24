package soft.divan.financemanager.core.shared_history_transaction_category.presenter.mapper

import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.model.CurrencyCode
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.model.symbol
import soft.divan.financemanager.core.shared_history_transaction_category.presenter.model.UiTransaction
import soft.divan.financemanager.feature.expenses_income_shared.presenter.mapper.toUi
import java.math.BigDecimal

fun Transaction.toUi(currency: CurrencyCode, category: Category): UiTransaction {
    return UiTransaction(
        id = this.id,
        accountId = this.accountId,
        category = category.toUi(),
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

fun UiTransaction.toDomain(): Transaction {
    return Transaction(
        id = this.id,
        accountId = this.accountId,
        categoryId = this.category.id,
        amount = this.amount,
        transactionDate = this.transactionDate,
        comment = this.comment,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}