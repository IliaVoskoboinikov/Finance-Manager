package soft.divan.financemanager.feature.security.security_impl.presenter.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
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

    var pinEntry by mutableStateOf("")
        private set

    var confirmPin by mutableStateOf("")
        private set

    var step by mutableStateOf(1) // 1 - ввод пина, 2 - подтверждение

    var error by mutableStateOf<String?>(null)

    var success by mutableStateOf(false)
        private set

    fun onDigitEntered(digit: Char) {
        if (step == 1 && pinEntry.length < 4) {
            pinEntry += digit
            if (pinEntry.length == 4) {
                step = 2
            }
        } else if (step == 2 && confirmPin.length < 4) {
            confirmPin += digit
            if (confirmPin.length == 4) {
                validateAndSave()
            }
        }
    }

    fun onDelete() {
        if (step == 2 && confirmPin.isNotEmpty()) {
            confirmPin = confirmPin.dropLast(1)
        } else if (step == 1 && pinEntry.isNotEmpty()) {
            pinEntry = pinEntry.dropLast(1)
        }
    }

    private fun validateAndSave() {
        if (pinEntry == confirmPin) {
            savePinUseCase(pinEntry)
            success = true
        } else {
            error = "PIN-коды не совпадают"
            confirmPin = ""
        }
    }
}
