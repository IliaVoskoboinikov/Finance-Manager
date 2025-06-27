package soft.divan.financemanager.presenter.ui.model

import soft.divan.financemanager.domain.model.Transaction
import soft.divan.financemanager.domain.util.DateHelper

sealed class HistoryUiState {
    data object Loading : HistoryUiState()
    data class Success(
        val transactions: List<Transaction>,
        val sumTransaction: String,

        val startDate: String = DateHelper.getCurrentMonthStartDisplayFormat(),
        val endDate: String = DateHelper.getTodayDisplayFormat(),
    ) : HistoryUiState()

    data class Error(val message: String) : HistoryUiState()
}

sealed class HistoryUiItemList {
    data class Item(
        val emoji: String,
        val content: String,
        val subContent: String,
        val price: String,
        val time: String,
        val onClick: () -> Unit
    ) : HistoryUiItemList()

}