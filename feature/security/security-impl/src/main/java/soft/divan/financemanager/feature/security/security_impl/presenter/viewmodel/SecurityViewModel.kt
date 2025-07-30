package soft.divan.financemanager.feature.security.security_impl.presenter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import soft.divan.financemanager.feature.security.security_impl.domain.usecase.DeletePinUseCase
import soft.divan.financemanager.feature.security.security_impl.domain.usecase.GetSavedPinUseCase
import soft.divan.financemanager.feature.security.security_impl.domain.usecase.IsPinSetUseCase
import soft.divan.financemanager.feature.security.security_impl.domain.usecase.SavePinUseCase
import javax.inject.Inject

@HiltViewModel
open class SecurityViewModel @Inject constructor(
    private val getSavedPinUseCase: GetSavedPinUseCase,
    private val isPinSetUseCase: IsPinSetUseCase,
    private val savePinUseCase: SavePinUseCase,
    private val deletePinUseCase: DeletePinUseCase
) : ViewModel() {

    private val _pin = MutableStateFlow<String>("")
    val pin: StateFlow<String> = _pin
        .onStart { loadPin() }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000L),
            ""
        )

    fun loadPin() {
        _pin.update { getSavedPinUseCase() ?: "" }
    }

    fun deletePin() {
        deletePinUseCase()
        _pin.value = ""
    }

}
