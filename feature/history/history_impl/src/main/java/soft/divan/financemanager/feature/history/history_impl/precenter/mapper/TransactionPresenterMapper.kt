package soft.divan.financemanager.feature.history.history_impl.precenter.mapper


import soft.divan.financemanager.core.domain.model.Category
import soft.divan.financemanager.core.domain.model.CurrencyCode
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.model.formatWith
import soft.divan.financemanager.feature.history.history_impl.precenter.model.UiTransaction

//todo
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