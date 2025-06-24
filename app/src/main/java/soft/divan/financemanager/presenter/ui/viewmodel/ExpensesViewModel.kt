package soft.divan.financemanager.presenter.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import soft.divan.financemanager.R
import javax.inject.Inject


sealed class ExpensesListItemUiModel {
    data class All(
        val content: Int,
        val trail: String
    ) : ExpensesListItemUiModel()

    data class ItemUi(
        val emoji: String,
        val content: String,
        val prise: String,
        val onClick: () -> Unit
    ) : ExpensesListItemUiModel()

    data class ItemUiSubContent(
        val emoji: String,
        val content: String,
        val subContent: String,
        val prise: String,
        val onClick: () -> Unit
    ) : ExpensesListItemUiModel()
}

sealed class ExpensesUiState {
    data object Loading : ExpensesUiState()
    data class Success(val items: List<ExpensesListItemUiModel>) : ExpensesUiState()
    data class Error(val message: String) : ExpensesUiState()
}

@HiltViewModel
class ExpensesViewModel @Inject constructor(): ViewModel() {

    private val _uiState = MutableStateFlow<ExpensesUiState>(ExpensesUiState.Loading)
    val uiState: StateFlow<ExpensesUiState> = _uiState.asStateFlow()

    init {
        loadExpenses()
    }

    private val mockExpenses = listOf(
        ExpensesListItemUiModel.All(
            content = R.string.all,
            trail = "436 558 ₽"
        ),
        ExpensesListItemUiModel.ItemUi(
            emoji = "\uD83C\uDFE1",
            content = "Аренда квартиры",
            prise = "100 000 ₽",
            onClick = {}
        ),
        ExpensesListItemUiModel.ItemUi(
            emoji = "\uD83D\uDC57",
            content = "Одежда",
            prise = "100 000 ₽",
            onClick = {}
        ),
        ExpensesListItemUiModel.ItemUiSubContent(
            emoji = "\uD83D\uDC36",
            content = "На собачку",
            subContent = "Джек",
            prise = "100 000 ₽",
            onClick = {}
        ),
        ExpensesListItemUiModel.ItemUiSubContent(
            emoji = "\uD83D\uDC36",
            content = "На собачку",
            subContent = "Энни",
            prise = "100 000 ₽",
            onClick = {}
        ),
        ExpensesListItemUiModel.ItemUi(
            emoji = "pk",
            content = "Ремонт квартиры",
            prise = "100 000 ₽",
            onClick = {}
        ),
        ExpensesListItemUiModel.ItemUi(
            emoji = "\uD83C\uDF6D",
            content = "Продукты",
            prise = "100 000 ₽",
            onClick = {}
        ),
        ExpensesListItemUiModel.ItemUi(
            emoji = "\uD83C\uDFCB\uFE0F",
            content = "Спортзал",
            prise = "100 000 ₽",
            onClick = {}
        ),
        ExpensesListItemUiModel.ItemUi(
            emoji = "\uD83D\uDC8A",
            content = "Медицина",
            prise = "100 000 ₽",
            onClick = {}
        )
    )

    private fun loadExpenses() {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = ExpensesUiState.Success(listOf())
        }
    }
}
