package soft.divan.financemanager.feature.security.security_impl.presenter.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import soft.divan.financemanager.feature.security.security_impl.domain.usecase.GetSavedPinUseCase
import soft.divan.financemanager.feature.security.security_impl.domain.usecase.IsPinSetUseCase
import soft.divan.financemanager.feature.security.security_impl.domain.usecase.SavePinUseCase
import javax.inject.Inject

@HiltViewModel
open class SecurityViewModel @Inject constructor(
    private val getSavedPinUseCase: GetSavedPinUseCase,
    private val isPinSetUseCase: IsPinSetUseCase,
    private val savePinUseCase: SavePinUseCase
) : ViewModel() {

    private val _pin = MutableStateFlow<String>("")
    val pin: StateFlow<String> = _pin.asStateFlow()

    init {
        _pin.value = getSavedPinUseCase() ?: ""
    }

}
