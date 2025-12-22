package soft.divan.financemanager.feature.transaction.transaction_impl.precenter.mapper


import soft.divan.financemanager.core.domain.data.DateHelper
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.model.CategoryUi
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.model.TransactionMode
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.model.TransactionUi

fun Transaction.toUi(category: CategoryUi): TransactionUi {
    return TransactionUi(
        id = idServer,
        accountId = accountId,
        category = category,
        amount = amount.stripTrailingZeros().toPlainString(),
        date = DateHelper.formatDateForDisplay(transactionDate.toLocalDate()),
        time = DateHelper.formatTimeForDisplay(transactionDate),
        comment = comment.toString(),
        createdAt = DateHelper.formatTimeForDisplay(createdAt),
        updatedAt = DateHelper.formatTimeForDisplay(updatedAt),
        currencyCode = currencyCode,
        mode = TransactionMode.Edit(idServer)
    )
}

fun TransactionUi.toDomain(): Transaction {
    return Transaction(
        //todo
        idServer = id ?: -1,
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