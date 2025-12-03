package soft.divan.financemanager.feature.security.security_impl.presenter.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import soft.divan.financemanager.feature.security.security_impl.domain.usecase.SavePinUseCase
import soft.divan.financemanager.feature.security.security_impl.presenter.model.CreatePinScreenState
import javax.inject.Inject

@HiltViewModel
class CreatePinViewModel @Inject constructor(
    private val savePinUseCase: SavePinUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<CreatePinScreenState>(CreatePinScreenState.InitialState)
    val uiState: StateFlow<CreatePinScreenState> = _uiState.asStateFlow()

    fun changeState(state: CreatePinScreenState) {
        _uiState.value = state
    }

    fun savePinCode(pin: String) {
        savePinUseCase(pin)
        changeState(CreatePinScreenState.PinCreatedState)
    }

}