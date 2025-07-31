package soft.divan.financemanager.feature.security.security_impl.presenter.screen

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import soft.divan.financemanager.feature.security.security_impl.R
import soft.divan.financemanager.feature.security.security_impl.presenter.data.CreatePinScreenState
import soft.divan.financemanager.feature.security.security_impl.presenter.viewmodel.CreatePinViewModel


@Composable
fun CreatePinScreen(
    viewModel: CreatePinViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {

    var headerId by remember { mutableIntStateOf(R.string.сome_up_with_pin) }
    var tempPin by remember { mutableStateOf<String?>(null) }
    val notificationText = ""
    var notification by remember { mutableStateOf(notificationText) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()


    when (val state = uiState) {

        is CreatePinScreenState.InitialState -> {
            headerId = R.string.сome_up_with_pin
            tempPin = null
        }

        is CreatePinScreenState.EnteringPinState -> {
            headerId = R.string.repeat_pin
            tempPin = state.pin
            notification = ""
        }

        is CreatePinScreenState.ConfirmingPinState -> {
            if (tempPin == state.pin) {
                viewModel.savePinCode(tempPin!!)
                viewModel.changeState(CreatePinScreenState.PinCreatedState)
            } else {
                notification = stringResource(id = R.string.pin_codes_do_not_match)
                viewModel.changeState(CreatePinScreenState.InitialState)
            }
        }

        is CreatePinScreenState.PinCreatedState -> {
            onNavigateBack()
        }

        is CreatePinScreenState.ErrorState -> {

        }

    }

    PinEntryCommonScreen(
        titleId = headerId,
        errorMessage = notification,
        onPinEntered = { pinCode ->
            if (tempPin == null) {
                viewModel.changeState(CreatePinScreenState.EnteringPinState(pinCode))
            } else {
                viewModel.changeState(CreatePinScreenState.ConfirmingPinState(pinCode))
            }
        },
        )

}
