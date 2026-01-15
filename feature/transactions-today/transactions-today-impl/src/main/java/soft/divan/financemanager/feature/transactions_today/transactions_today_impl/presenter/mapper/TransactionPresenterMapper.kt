package soft.divan.financemanager.feature.transactions_today.transactions_today_impl.presenter.mapper

import soft.divan.financemanager.core.domain.data.DateHelper
import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.model.CurrencySymbol
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.feature.transactions_today.transactions_today_impl.presenter.model.TransactionUi

fun Transaction.toUi(category: Category): TransactionUi {
    return TransactionUi(
        id = id,
        category = category.toUi(),
        amountFormatted = amount.stripTrailingZeros().toPlainString() + " " + CurrencySymbol.fromCode(
            currencyCode
        ),
        transactionDateTime = DateHelper.formatDateTimeForDisplay(transactionDate),
        comment = comment.toString(),
    )
}
