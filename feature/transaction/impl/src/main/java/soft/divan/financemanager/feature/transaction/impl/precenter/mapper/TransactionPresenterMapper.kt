package soft.divan.financemanager.feature.transaction.impl.precenter.mapper

import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.utli.UiDateFormatter
import soft.divan.financemanager.feature.transaction.impl.precenter.model.CategoryUi
import soft.divan.financemanager.feature.transaction.impl.precenter.model.TransactionMode
import soft.divan.financemanager.feature.transaction.impl.precenter.model.TransactionUi

fun Transaction.toUi(category: CategoryUi): TransactionUi {
    return TransactionUi(
        id = id,
        accountId = accountLocalId,
        category = category,
        amount = amount.stripTrailingZeros().toPlainString(),
        date = UiDateFormatter.formatDate(transactionDate),
        time = UiDateFormatter.formatTime(transactionDate),
        comment = comment.toString(),
        createdAt = UiDateFormatter.formatDateTime(createdAt),
        updatedAt = UiDateFormatter.formatDateTime(updatedAt),
        currencyCode = currencyCode,
        mode = TransactionMode.Edit(id)
    )
}

fun TransactionUi.toDomain(): Transaction {
    return Transaction(
        id = id,
        accountLocalId = accountId,
        categoryId = category.id,
        amount = amount.toBigDecimal(),
        transactionDate = UiDateFormatter.parse(date, time),
        comment = comment,
        createdAt = UiDateFormatter.parse(createdAt),
        updatedAt = UiDateFormatter.parse(updatedAt),
        currencyCode = currencyCode,
    )
}
