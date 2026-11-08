package soft.divan.financemanager.feature.transaction.impl.precenter.model

import soft.divan.financemanager.core.domain.model.TransactionType
import java.time.Instant

/**
 * Состояние формы операции.
 *
 * Метки времени хранятся как [Instant], а в строки превращаются только при отрисовке: строка
 * `dd.MM.yyyy HH:mm` теряет секунды, и правка, собранная из неё обратно, проигрывала бы
 * last-write-wins серверной версии той же минуты.
 */
data class TransactionUi(
    val id: String,
    val accountId: String,
    val targetAccountLocalId: String?, // только для transfer
    val category: CategoryUi,
    val currencyId: String,
    val amount: String,
    val type: TransactionType,
    val transactionDate: Instant,
    val comment: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val mode: TransactionMode
)
