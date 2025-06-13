package soft.divan.financemanager.presenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import soft.divan.financemanager.R


sealed class ExpensesListItemModel {
    data class All(
        val content: Int,
        val trail: String
    ) : ExpensesListItemModel()

    data class Item(
        val emoji: String,
        val content: String,
        val prise: String,
        val onClick: () -> Unit
    ) : ExpensesListItemModel()

    data class ItemSubContent(
        val emoji: String,
        val content: String,
        val subContent: String,
        val prise: String,
        val onClick: () -> Unit
    ) : ExpensesListItemModel()
}

sealed class ExpensesUiState {
    data object Loading : ExpensesUiState()
    data class Success(val items: List<ExpensesListItemModel>) : ExpensesUiState()
    data class Error(val message: String) : ExpensesUiState()
}


class ExpensesViewModel(private val dispatcher: CoroutineDispatcher = Dispatchers.IO) : ViewModel() {

    private val _uiState = MutableStateFlow<ExpensesUiState>(ExpensesUiState.Loading)
    val uiState: StateFlow<ExpensesUiState> = _uiState.asStateFlow()

    init {
        loadExpenses()
    }

    private val mockExpenses = listOf(
        ExpensesListItemModel.All(
            content = R.string.all,
            trail = "436 558 ₽"
        ),
        ExpensesListItemModel.Item(
            emoji = "\uD83C\uDFE1",
            content = "Аренда квартиры",
            prise = "100 000 ₽",
            onClick = {}
        ),
        ExpensesListItemModel.Item(
            emoji = "\uD83D\uDC57",
            content = "Одежда",
            prise = "100 000 ₽",
            onClick = {}
        ),
        ExpensesListItemModel.ItemSubContent(
            emoji = "\uD83D\uDC36",
            content = "На собачку",
            subContent = "Джек",
            prise = "100 000 ₽",
            onClick = {}
        ),
        ExpensesListItemModel.ItemSubContent(
            emoji = "\uD83D\uDC36",
            content = "На собачку",
            subContent = "Энни",
            prise = "100 000 ₽",
            onClick = {}
        ),
        ExpensesListItemModel.Item(
            emoji = "pk",
            content = "Ремонт квартиры",
            prise = "100 000 ₽",
            onClick = {}
        ),
        ExpensesListItemModel.Item(
            emoji = "\uD83C\uDF6D",
            content = "Продукты",
            prise = "100 000 ₽",
            onClick = {}
        ),
        ExpensesListItemModel.Item(
            emoji = "\uD83C\uDFCB\uFE0F",
            content = "Спортзал",
            prise = "100 000 ₽",
            onClick = {}
        ),
        ExpensesListItemModel.Item(
            emoji = "\uD83D\uDC8A",
            content = "Медицина",
            prise = "100 000 ₽",
            onClick = {}
        )
    )

    private fun loadExpenses() {
        viewModelScope.launch(dispatcher) {
            _uiState.value = ExpensesUiState.Success(mockExpenses)
        }
    }
}
