package soft.divan.financemanager.feature.security.impl.presenter.screen

import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow
import androidx.compose.ui.Modifier
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import soft.divan.financemanager.feature.security.impl.R
import soft.divan.financemanager.feature.security.impl.presenter.model.PinLockEvent
import soft.divan.financemanager.feature.security.impl.presenter.model.PinLockMessage
import soft.divan.financemanager.feature.security.impl.presenter.model.PinLockUiState
import soft.divan.financemanager.feature.security.impl.presenter.model.message
import soft.divan.financemanager.feature.security.impl.presenter.util.BiometricAuthenticator
import soft.divan.financemanager.feature.security.impl.presenter.util.BiometricTexts
import soft.divan.financemanager.feature.security.impl.presenter.util.findFragmentActivity
import soft.divan.financemanager.feature.security.impl.presenter.viewmodel.PinLockViewModel
import java.time.Duration
import java.time.Instant
import java.util.Locale

// Превью — в PinLockScreenPreview.kt: этот файл тянет hiltViewModel и BiometricPrompt, а превью
// должно рисоваться без DI.

private const val SECOND_MS = 1_000L
private const val SECONDS_IN_MINUTE = 60

/**
 * Экран замка: и замок интерфейса, и разблокировка данных уровня PIN.
 *
 * Показывается хостом поверх всего приложения, вне навигации. [onUnlock] вызывается, когда
 * можно входить, — и после стирания данных («забыл PIN», исчерпанные попытки): тогда хост
 * покажет экран входа.
 */
@Composable
fun PinLockScreen(
    onUnlock: () -> Unit,
    viewModel: PinLockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val authenticator =
        remember(context) { context.findFragmentActivity()?.let(::BiometricAuthenticator) }
    val deviceSupportsBiometric = remember(authenticator, uiState.dataProtectedByPin) {
        authenticator != null &&
            if (uiState.dataProtectedByPin) authenticator.canUseStrong() else authenticator.canUseAny()
    }
    val showBiometric = uiState.biometricEnabled && deviceSupportsBiometric
    var autoPrompted by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.onShown() }
    PinLockEvents(
        events = viewModel.events,
        authenticator = authenticator,
        onUnlock = onUnlock,
        onBiometricSuccess = viewModel::onBiometricSucceeded,
        onBiometricFailure = viewModel::onBiometricFailed
    )

    // Как и раньше, биометрия предлагается сразу при показе замка — один раз
    LaunchedEffect(showBiometric) {
        if (showBiometric && !autoPrompted) {
            autoPrompted = true
            viewModel.onBiometricRequested()
        }
    }

    PinLockContent(
        uiState = uiState,
        lockoutSecondsLeft = rememberLockoutSecondsLeft(
            lockedUntil = uiState.status.lockedUntil,
            onExpire = viewModel::onLockoutExpired
        ),
        showBiometricButton = showBiometric,
        actions = PinLockActions(
            onPinEntered = viewModel::onPinEntered,
            onBiometricClick = viewModel::onBiometricRequested,
            onForgotPinClick = viewModel::onForgotPinClicked
        )
    )

    if (uiState.showForgotPinDialog) {
        ForgotPinDialog(
            isGuest = uiState.isGuest,
            onConfirm = viewModel::onForgotPinConfirmed,
            onDismiss = viewModel::onForgotPinDismissed
        )
    }
}

/** Одноразовые события замка: вход, стирание, системный запрос биометрии. */
@Composable
private fun PinLockEvents(
    events: Flow<PinLockEvent>,
    authenticator: BiometricAuthenticator?,
    onUnlock: () -> Unit,
    onBiometricSuccess: () -> Unit,
    onBiometricFailure: () -> Unit
) {
    val currentOnUnlock by rememberUpdatedState(onUnlock)
    val currentOnBiometricSuccess by rememberUpdatedState(onBiometricSuccess)
    val currentOnBiometricFailure by rememberUpdatedState(onBiometricFailure)
    val context = LocalContext.current
    val wipedMessage = stringResource(R.string.data_wiped)
    val biometricTexts = BiometricTexts(
        title = stringResource(R.string.authorization),
        subtitle = stringResource(R.string.use_biometry),
        negativeButton = stringResource(R.string.use_pin_instead)
    )
    LaunchedEffect(events) {
        events.collect { event ->
            when (event) {
                PinLockEvent.Unlocked -> currentOnUnlock()

                PinLockEvent.Wiped -> {
                    Toast.makeText(context, wipedMessage, Toast.LENGTH_LONG).show()
                    currentOnUnlock()
                }

                is PinLockEvent.ShowBiometricPrompt -> authenticator?.authenticate(
                    texts = biometricTexts,
                    cipher = event.cipher,
                    onSuccess = { currentOnBiometricSuccess() },
                    onFailure = { currentOnBiometricFailure() }
                ) ?: currentOnBiometricFailure()
            }
        }
    }
}

/** Действия экрана замка. */
data class PinLockActions(
    val onPinEntered: (String) -> Unit,
    val onBiometricClick: () -> Unit,
    val onForgotPinClick: () -> Unit
) {
    companion object {
        /** Пустые действия — для превью. */
        val NONE = PinLockActions(onPinEntered = {}, onBiometricClick = {}, onForgotPinClick = {})
    }
}

/**
 * Замок без DI — состояние приходит снаружи.
 *
 * @param lockoutSecondsLeft Сколько секунд ещё запрещён ввод; 0 — вводить можно.
 */
@Composable
fun PinLockContent(
    uiState: PinLockUiState,
    lockoutSecondsLeft: Long,
    showBiometricButton: Boolean,
    actions: PinLockActions,
    modifier: Modifier = Modifier
) {
    PinEntryCommonScreen(
        titleId = R.string.input_password,
        modifier = modifier,
        errorMessage = pinLockMessage(uiState, lockoutSecondsLeft),
        showBiometricButton = showBiometricButton,
        enabled = !uiState.inProgress && lockoutSecondsLeft <= 0,
        onPinEnter = actions.onPinEntered,
        onFingerprintClick = actions.onBiometricClick,
        footer = {
            TextButton(onClick = actions.onForgotPinClick, enabled = !uiState.inProgress) {
                Text(text = stringResource(R.string.forgot_pin))
            }
        }
    )
}

@Composable
private fun pinLockMessage(uiState: PinLockUiState, lockoutSecondsLeft: Long): String =
    when (val message = uiState.message(lockoutSecondsLeft)) {
        PinLockMessage.None -> ""

        is PinLockMessage.LockedOut -> stringResource(
            R.string.pin_locked_out,
            formatCountdown(message.secondsLeft)
        )

        PinLockMessage.LastAttempt -> stringResource(R.string.pin_last_attempt_warning)

        PinLockMessage.BiometricInvalidated -> stringResource(R.string.biometric_invalidated)

        is PinLockMessage.WrongPin -> when (val left = message.attemptsLeft) {
            null -> stringResource(R.string.wrong_pin)
            1 -> stringResource(R.string.wrong_pin_last_attempt)
            else -> pluralStringResource(R.plurals.wrong_pin_attempts_left, left, left)
        }
    }

/** Секунды до конца паузы; тикает раз в секунду и сообщает об окончании. */
@Composable
private fun rememberLockoutSecondsLeft(lockedUntil: Instant?, onExpire: () -> Unit): Long {
    val currentOnExpire by rememberUpdatedState(onExpire)
    val secondsLeft by produceState(initialValue = secondsUntil(lockedUntil), lockedUntil) {
        while (lockedUntil != null) {
            value = secondsUntil(lockedUntil)
            if (value <= 0) {
                currentOnExpire()
                break
            }
            delay(SECOND_MS)
        }
    }
    return secondsLeft
}

private fun secondsUntil(moment: Instant?): Long =
    moment?.let { Duration.between(Instant.now(), it).toMillis().plus(SECOND_MS - 1) / SECOND_MS } ?: 0

private fun formatCountdown(seconds: Long): String =
    String.format(Locale.ROOT, "%d:%02d", seconds / SECONDS_IN_MINUTE, seconds % SECONDS_IN_MINUTE)

/** Предупреждение перед сбросом PIN: без стирания данных сброс стал бы обходом замка. */
@Composable
fun ForgotPinDialog(
    isGuest: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.forgot_pin_title)) },
        text = {
            Text(
                stringResource(
                    if (isGuest) R.string.forgot_pin_message_guest else R.string.forgot_pin_message
                )
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = stringResource(R.string.wipe_data_action),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel_action)) }
        }
    )
}
