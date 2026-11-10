package soft.divan.financemanager.feature.security.impl.presenter.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
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
import soft.divan.financemanager.feature.security.impl.presenter.model.CreatePinError
import soft.divan.financemanager.feature.security.impl.presenter.model.CreatePinStep
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
private fun CreatePinScreenPreview() {
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

/**
 * Экран создания PIN: только рисует [CreatePinViewModel.uiState] и передаёт ввод — сценарий и
 * сохранение живут во ViewModel. [onNavigateBack] вызывается, когда PIN сохранён.
 */
@NavDestination(route = CreatePinKey::class)
@Composable
fun CreatePinScreen(
    viewModel: CreatePinViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnNavigateBack by rememberUpdatedState(onNavigateBack)

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted) currentOnNavigateBack()
    }

    PinEntryCommonScreen(
        titleId = when (uiState.step) {
            CreatePinStep.ENTER -> R.string.сome_up_with_pin
            CreatePinStep.CONFIRM -> R.string.repeat_pin
        },
        errorMessage = when (uiState.error) {
            CreatePinError.MISMATCH -> stringResource(R.string.pin_codes_do_not_match)
            CreatePinError.SAVE_FAILED -> stringResource(R.string.pin_save_failed)
            null -> ""
        },
        enabled = uiState.isInputEnabled,
        onPinEnter = viewModel::onPinEntered
    )
}
