package soft.divan.financemanager.presenter.ui.model

sealed class HistoryUiState {
    data object Loading : HistoryUiState()
    data class Success(val items: List<HistoryUiItemList>) : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
}

sealed class HistoryUiItemList {

    data class DateAndBalance(
        val content: Int,
        val trail: String,
    ) : HistoryUiItemList()

    data class Item(
        val emoji: String,
        val content: String,
        val subContent: String,
        val price: String,
        val time: String,
        val onClick: () -> Unit
    ) : HistoryUiItemList()

}