package soft.divan.financemanager.feature.transactions_today.transactions_today_impl.presenter.mapper


import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.model.CurrencySymbol
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.feature.transactions_today.transactions_today_impl.presenter.model.UiTransaction

fun Transaction.toUi(category: Category): UiTransaction {
    return UiTransaction(
        id = this.id,
        accountId = this.accountId,
        category = category.toUi(),
        amount = this.amount,
        amountFormatted = this.amount.toString() + " " + CurrencySymbol.fromCode(currencyCode),
        transactionDate = this.transactionDate,
        comment = this.comment.toString(),
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}
