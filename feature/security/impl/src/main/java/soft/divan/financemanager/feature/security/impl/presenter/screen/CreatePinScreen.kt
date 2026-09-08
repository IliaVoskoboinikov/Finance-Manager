package soft.divan.financemanager.feature.security.impl.presenter.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.skydoves.navgraph.annotations.NavDestination
import com.github.skydoves.navgraph.annotations.NavPreview
import soft.divan.financemanager.feature.security.api.CreatePinKey
import soft.divan.financemanager.feature.security.impl.R
import soft.divan.financemanager.feature.security.impl.presenter.components.Keyboard
import soft.divan.financemanager.feature.security.impl.presenter.components.PinCodeScreenHeader
import soft.divan.financemanager.feature.security.impl.presenter.components.RoundedBoxesRow
import soft.divan.financemanager.feature.security.impl.presenter.model.CreatePinScreenState
import soft.divan.financemanager.feature.security.impl.presenter.viewmodel.CreatePinViewModel
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme

/**
 * Превью экрана создания PIN для карты навигации ([CreatePinKey]).
 *
 * Собирается из тех же под-компонентов, что и реальный [PinEntryCommonScreen] (заголовок,
 * индикатор ввода, клавиатура), но без его параметра `BiometricPrompt.AuthenticationCallback`:
 * этот тип из `android.hardware.biometrics` не загружается в headless-рендере Layoutlib
 * (`NoClassDefFoundError`), поэтому сам `PinEntryCommonScreen` для превью непригоден.
 */
@NavPreview(route = CreatePinKey::class, primary = true)
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun CreatePinScreenPreview() {
    FinanceManagerTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            PinCodeScreenHeader(text = stringResource(R.string.сome_up_with_pin))
            RoundedBoxesRow(startQuantity = 4, quantity = 1)
            Keyboard(
                onNumberClick = {},
                onBackspaceClick = {}
            )
        }
    }
}

@NavDestination(route = CreatePinKey::class)
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
        }
    )
}
