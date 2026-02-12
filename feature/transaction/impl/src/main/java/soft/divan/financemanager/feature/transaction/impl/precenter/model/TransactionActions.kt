package soft.divan.financemanager.feature.transaction.impl.precenter.model

import java.time.LocalDate
import java.time.LocalTime

data class TransactionActions(
    val onNavigateBack: () -> Unit,
    val onSave: () -> Unit,
    val onAmountChange: (String) -> Unit,
    val onCommentChange: (String) -> Unit,
    val onDateChange: (LocalDate) -> Unit,
    val onTimeChange: (LocalTime) -> Unit,
    val onCategoryChange: (CategoryUi) -> Unit,
    val onAccountChange: (AccountUi) -> Unit,
    val onDelete: () -> Unit
)
