package soft.divan.financemanager.feature.account.impl.precenter.model

data class AccountActions(
    val onNavigateBack: () -> Unit,
    val onUpdateName: (String) -> Unit,
    val onUpdateBalance: (String) -> Unit,
    val onUpdateCurrency: (String) -> Unit,
    val onSave: () -> Unit,
    val onDelete: () -> Unit
)
