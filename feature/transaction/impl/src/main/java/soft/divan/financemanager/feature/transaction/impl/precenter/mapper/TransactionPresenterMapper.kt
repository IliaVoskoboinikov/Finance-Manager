package soft.divan.financemanager.feature.transaction.impl.precenter.mapper

import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.feature.transaction.impl.precenter.model.CategoryUi
import soft.divan.financemanager.feature.transaction.impl.precenter.model.TransactionMode
import soft.divan.financemanager.feature.transaction.impl.precenter.model.TransactionUi

fun Transaction.toUi(category: CategoryUi): TransactionUi {
    return TransactionUi(
        id = id,
        accountId = accountLocalId,
        category = category,
        amount = amount.stripTrailingZeros().toPlainString(),
        transactionDate = transactionDate,
        comment = comment.orEmpty(),
        createdAt = createdAt,
        updatedAt = updatedAt,
        currencyId = currencyId,
        mode = TransactionMode.Edit(id),
        targetAccountLocalId = targetAccountLocalId,
        type = type
    )
}

fun TransactionUi.toDomain(): Transaction {
    return Transaction(
        id = id,
        accountLocalId = accountId,
        categoryId = category.id,
        amount = amount.toBigDecimal(),
        transactionDate = transactionDate,
        comment = comment,
        createdAt = createdAt,
        updatedAt = updatedAt,
        currencyId = currencyId,
        targetAccountLocalId = targetAccountLocalId,
        type = type
    )
}
