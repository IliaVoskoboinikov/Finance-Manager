package soft.divan.financemanager.feature.analysis.analysis_impl.precenter.viewModel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import soft.divan.financemanager.feature.analysis.analysis_impl.precenter.model.AnalysisUiState
import javax.inject.Inject

@HiltViewModel
class AnalysisViewModel @Inject constructor(

) : ViewModel() {
    private val _uiState = MutableStateFlow<AnalysisUiState>(AnalysisUiState.Loading)
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

}