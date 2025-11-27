package soft.divan.financemanager.feature.transaction.transaction_impl.precenter.mapper


import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.model.CurrencySymbol
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.feature.transaction.transaction_impl.precenter.model.UiTransaction

fun Transaction.toUi(category: Category): UiTransaction {
    return UiTransaction(
        id = id,
        accountId = accountId,
        category = category.toUi(),
        amount = amount,
        amountFormatted = amount.toString() + " " + CurrencySymbol.fromCode(currencyCode),
        transactionDate = transactionDate,
        comment = comment.toString(),
        createdAt = createdAt,
        updatedAt = updatedAt,
        currencyCode = currencyCode,
    )
}

fun UiTransaction.toDomain(): Transaction {
    return Transaction(
        id = id,
        accountId = accountId,
        categoryId = category.id,
        amount = amount,
        transactionDate = transactionDate,
        comment = comment,
        createdAt = createdAt,
        updatedAt = updatedAt,
        currencyCode = currencyCode,
    )
}