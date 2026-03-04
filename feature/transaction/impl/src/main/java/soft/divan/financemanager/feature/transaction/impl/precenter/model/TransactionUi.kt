package soft.divan.financemanager.feature.transaction.impl.precenter.model

import soft.divan.financemanager.core.domain.model.TransactionType

data class TransactionUi(
    val id: String,
    val accountId: String,
    val targetAccountLocalId: String?, // только для transfer
    val category: CategoryUi,
    val currencyCode: String,
    val amount: String,
    val type: TransactionType,
    val date: String,
    val time: String,
    val comment: String,
    val createdAt: String,
    val updatedAt: String,
    val mode: TransactionMode
)
