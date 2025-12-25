package soft.divan.financemanager.feature.languages.languages_impl.precenter.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import soft.divan.financemanager.feature.languages.languages_impl.R
import soft.divan.financemanager.feature.languages.languages_impl.domain.usecase.ObserveLanguagesUseCase
import soft.divan.financemanager.feature.languages.languages_impl.domain.usecase.SetLanguageUseCase
import soft.divan.financemanager.feature.languages.languages_impl.precenter.mapper.toDomain
import soft.divan.financemanager.feature.languages.languages_impl.precenter.mapper.toUi
import soft.divan.financemanager.feature.languages.languages_impl.precenter.model.LanguageUi
import soft.divan.financemanager.feature.languages.languages_impl.precenter.model.LanguageUiState
import javax.inject.Inject

@HiltViewModel
class LanguagesViewModel @Inject constructor(
    private val setLanguageUseCase: SetLanguageUseCase,
    private val observeLanguagesUseCase: ObserveLanguagesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<LanguageUiState>(LanguageUiState.Loading)
    val uiState: StateFlow<LanguageUiState> = _uiState.asStateFlow()

    init {
        observe()
    }

    fun observe() {
        viewModelScope.launch {
            observeLanguagesUseCase()
                .onEach { _uiState.value = LanguageUiState.Success(it.toUi()) }
                .catch { _uiState.value = LanguageUiState.Error(R.string.error) }
                .launchIn(viewModelScope)
        }
    }

    fun onLanguageSelected(languages: LanguageUi) {
        viewModelScope.launch {
            setLanguageUseCase(languages.toDomain())
        }
    }

}