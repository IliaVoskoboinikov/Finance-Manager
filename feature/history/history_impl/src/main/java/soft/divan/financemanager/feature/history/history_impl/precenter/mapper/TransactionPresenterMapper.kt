package soft.divan.financemanager.feature.history.history_impl.precenter.mapper


import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.model.CurrencySymbol
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.util.DateHelper
import soft.divan.financemanager.feature.history.history_impl.precenter.model.UiTransaction

fun Transaction.toUi(category: Category): UiTransaction {
    return UiTransaction(
        id = id,
        category = category.toUi(),
        amountFormatted = amount.stripTrailingZeros().toPlainString() + " " + CurrencySymbol.fromCode(
            currencyCode
        ),
        transactionDateTime = DateHelper.formatDateTimeForDisplay(transactionDate),
        comment = comment.toString(),
    )
}
