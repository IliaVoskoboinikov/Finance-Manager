package soft.divan.financemanager.feature.security.security_impl.presenter.screen

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import soft.divan.financemanager.feature.security.security_impl.presenter.data.CreatePinScreenState

import soft.divan.financemanager.feature.security.security_impl.presenter.viewmodel.CreatePinViewModel


@Composable
fun CreatePinScreen(
    viewModel: CreatePinViewModel = hiltViewModel(),
    onNavegateBack: () -> Unit
) {


    var headerId by remember { mutableStateOf("придумайте пин") }
    var newPin by remember { mutableStateOf<String?>(null) }
    var tempPin by remember { mutableStateOf<String?>(null) }

    //   val notificationText = stringResource(id = R.string.empty)
    //  var notification by remember { mutableStateOf(notificationText) }
    //  val forgotMessageId by remember { mutableIntStateOf(R.string.pin_code_forgot_text) }
    var isPinEntering by remember { mutableStateOf(false) }
    var clearTrigger by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (val state = uiState) {

        is CreatePinScreenState.InitialState -> {
            headerId = "придумайте пин"
            isPinEntering = false
        }

        is CreatePinScreenState.EnteringPinState -> {
            val pin = (uiState as CreatePinScreenState.EnteringPinState).pin
            headerId = "повторите пин"
            tempPin = pin
            // notification = stringResource(id = R.string.empty)
        }

        is CreatePinScreenState.ConfirmingPinState -> {
            if (tempPin == (uiState as CreatePinScreenState.ConfirmingPinState).pin) {
                viewModel.savePinCode(tempPin!!)
                viewModel.changeState(CreatePinScreenState.PinCreatedState)
            } else {
                // notification = stringResource(id = R.string.pin_codes_do_not_match)
                viewModel.changeState(CreatePinScreenState.InitialState)
            }
        }

        is CreatePinScreenState.PinCreatedState -> {
            Log.e("1312ddw", "dd")
            onNavegateBack()

        }

        is CreatePinScreenState.ErrorState -> {

        }

    }

    PinEntryScreen(
        title = headerId,
        clearPinTrigger = clearTrigger,
        onPinEntered = { pinCode ->
            if (tempPin == null) {
                viewModel.changeState(CreatePinScreenState.EnteringPinState(pinCode))
                clearTrigger = true
            } else {
                viewModel.changeState(CreatePinScreenState.ConfirmingPinState(pinCode))
            }
        },

        )

}
