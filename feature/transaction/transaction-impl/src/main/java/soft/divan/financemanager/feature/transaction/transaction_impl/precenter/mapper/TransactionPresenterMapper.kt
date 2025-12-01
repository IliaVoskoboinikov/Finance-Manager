package soft.divan.financemanager.feature.transaction.transaction_impl.precenter.mapper


import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.util.DateHelper
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.model.TransactionMode
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.model.UiTransaction

fun Transaction.toUi(category: Category): UiTransaction {
    return UiTransaction(
        id = id,
        accountId = accountId,
        category = category.toUi(),
        amount = amount.stripTrailingZeros().toPlainString(),
        date = DateHelper.formatDateForDisplay(transactionDate.toLocalDate()),
        time = DateHelper.formatTimeForDisplay(transactionDate),
        comment = comment.toString(),
        createdAt = DateHelper.formatTimeForDisplay(createdAt),
        updatedAt = DateHelper.formatTimeForDisplay(updatedAt),
        currencyCode = currencyCode,
        mode = TransactionMode.Edit(id)
    )
}

fun UiTransaction.toDomain(): Transaction {
    return Transaction(
        id = id ?: -1,
        accountId = accountId,
        categoryId = category.id,
        amount = amount.toBigDecimal(),
        transactionDate = DateHelper.parseDisplayDateTime("$date $time"),
        comment = comment,
        createdAt = DateHelper.parseDisplayDateTime(createdAt),
        updatedAt = DateHelper.parseDisplayDateTime(updatedAt),
        currencyCode = currencyCode,
    )
}