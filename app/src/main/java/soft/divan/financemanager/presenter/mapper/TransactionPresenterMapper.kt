package soft.divan.financemanager.presenter.mapper

import soft.divan.financemanager.domain.model.Transaction
import soft.divan.financemanager.presenter.ui.model.UiTransaction
import java.math.BigDecimal

fun Transaction.toUi(): UiTransaction {
    return UiTransaction(
        id = this.id,
        accountId = this.accountId,
        category = this.category.toUi(),
        amount = this.amount,
        amountFormatted = formatAmount(this.amount),
        transactionDate = this.transactionDate,
        comment = this.comment,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

/**Вспомогательная функция форматирования суммы */
fun formatAmount(amount: BigDecimal): String {
    // Можно добавить локализацию, валюту и пр.
    return "${amount.toPlainString()} ₽"
}