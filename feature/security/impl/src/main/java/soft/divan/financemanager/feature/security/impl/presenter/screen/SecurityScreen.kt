package soft.divan.financemanager.feature.security.impl.presenter.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.skydoves.navgraph.annotations.NavDestination
import com.github.skydoves.navgraph.annotations.NavEdge
import com.github.skydoves.navgraph.annotations.NavPreview
import soft.divan.financemanager.core.domain.model.DataProtectionLevel
import soft.divan.financemanager.feature.security.api.CreatePinKey
import soft.divan.financemanager.feature.security.api.SecurityKey
import soft.divan.financemanager.feature.security.impl.R
import soft.divan.financemanager.feature.security.impl.domain.model.SecuritySettings
import soft.divan.financemanager.feature.security.impl.presenter.components.PinPadDialog
import soft.divan.financemanager.feature.security.impl.presenter.components.PinSection
import soft.divan.financemanager.feature.security.impl.presenter.components.PinSectionActions
import soft.divan.financemanager.feature.security.impl.presenter.components.ProtectionLevelSection
import soft.divan.financemanager.feature.security.impl.presenter.components.SectionHeader
import soft.divan.financemanager.feature.security.impl.presenter.components.SecurityDialogHost
import soft.divan.financemanager.feature.security.impl.presenter.components.SettingSwitch
import soft.divan.financemanager.feature.security.impl.presenter.model.SecurityEvent
import soft.divan.financemanager.feature.security.impl.presenter.model.SecurityFlowState
import soft.divan.financemanager.feature.security.impl.presenter.model.SecurityMessage
import soft.divan.financemanager.feature.security.impl.presenter.model.SecurityUiState
import soft.divan.financemanager.feature.security.impl.presenter.util.BiometricAuthenticator
import soft.divan.financemanager.feature.security.impl.presenter.util.BiometricTexts
import soft.divan.financemanager.feature.security.impl.presenter.util.findFragmentActivity
import soft.divan.financemanager.feature.security.impl.presenter.viewmodel.BiometricSettingsViewModel
import soft.divan.financemanager.feature.security.impl.presenter.viewmodel.PinSettingsViewModel
import soft.divan.financemanager.feature.security.impl.presenter.viewmodel.SecurityViewModel
import soft.divan.financemanager.uikit.components.ErrorContent
import soft.divan.financemanager.uikit.components.LoadingProgressBar
import soft.divan.financemanager.uikit.components.TopBar
import soft.divan.financemanager.uikit.icons.ArrowBack
import soft.divan.financemanager.uikit.model.TopBarModel
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme

@NavPreview(route = SecurityKey::class, primary = true)
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PreviewSecurityScreen() {
    FinanceManagerTheme {
        SecurityContent(
            uiState = SecurityUiState.Success(
                settings = SecuritySettings(
                    level = DataProtectionLevel.DEVICE,
                    hasPin = true,
                    biometricEnabled = false,
                    secureScreen = true,
                    isGuest = false
                ),
                biometricAvailable = true
            ),
            busy = false,
            actions = SecurityScreenActions.NONE
        )
    }
}

@Preview(name = "PIN level — dark", showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun PreviewSecurityScreenPinLevel() {
    FinanceManagerTheme(darkTheme = true) {
        SecurityContent(
            uiState = SecurityUiState.Success(
                settings = SecuritySettings(
                    level = DataProtectionLevel.PIN,
                    hasPin = true,
                    biometricEnabled = true,
                    secureScreen = false,
                    isGuest = true
                ),
                biometricAvailable = true
            ),
            busy = true,
            actions = SecurityScreenActions.NONE
        )
    }
}

/** Действия экрана настроек безопасности. */
data class SecurityScreenActions(
    val onNavigateBack: () -> Unit,
    val onLevelSelected: (DataProtectionLevel) -> Unit,
    val onBiometricToggled: (Boolean) -> Unit,
    val onSecureScreenToggled: (Boolean) -> Unit,
    val pin: PinSectionActions
) {
    companion object {
        /** Пустые действия — для превью. */
        val NONE = SecurityScreenActions(
            onNavigateBack = {},
            onLevelSelected = {},
            onBiometricToggled = {},
            onSecureScreenToggled = {},
            pin = PinSectionActions(onCreate = {}, onChange = {}, onDelete = {})
        )
    }
}

@NavDestination(route = SecurityKey::class)
@NavEdge(to = CreatePinKey::class, label = "установить PIN")
@Composable
fun SecurityScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit,
    onNavigateToCreatePin: () -> Unit,
    viewModel: SecurityViewModel = hiltViewModel(),
    pinViewModel: PinSettingsViewModel = hiltViewModel(),
    biometricViewModel: BiometricSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pinFlow by pinViewModel.state.collectAsStateWithLifecycle()
    val biometricFlow by biometricViewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val success = uiState as? SecurityUiState.Success

    BiometricEffects(viewModel, biometricViewModel)
    MessageEffect(snackbarHostState, success?.message, viewModel::onMessageShown)
    MessageEffect(snackbarHostState, pinFlow.message, pinViewModel::onMessageShown)
    MessageEffect(snackbarHostState, biometricFlow.message, biometricViewModel::onMessageShown)

    val dataProtectedByPin = success?.settings?.level == DataProtectionLevel.PIN
    SecurityContent(
        modifier = modifier,
        uiState = uiState,
        busy = success?.inProgress == true || pinFlow.inProgress || biometricFlow.inProgress,
        snackbarHostState = snackbarHostState,
        actions = SecurityScreenActions(
            onNavigateBack = onNavigateBack,
            onLevelSelected = viewModel::onLevelSelected,
            onBiometricToggled = biometricViewModel::onToggled,
            onSecureScreenToggled = viewModel::onSecureScreenToggled,
            pin = PinSectionActions(
                onCreate = onNavigateToCreatePin,
                onChange = pinViewModel::onChangePinClicked,
                onDelete = { pinViewModel.onDeletePinClicked(dataProtectedByPin) }
            )
        )
    )

    // Диалоги и ввод PIN поверх экрана: одновременно открыт не больше одного
    success?.let { LevelOverlays(it, viewModel) }
    FlowPinPad(pinFlow, pinViewModel::onPinEntered, pinViewModel::onDismissed)
    FlowPinPad(biometricFlow, biometricViewModel::onPinEntered, biometricViewModel::onDismissed)
}

/** Системный запрос биометрии и её включение после включения уровня PIN. */
@Composable
private fun BiometricEffects(
    viewModel: SecurityViewModel,
    biometricViewModel: BiometricSettingsViewModel
) {
    val context = LocalContext.current
    val authenticator =
        remember(context) { context.findFragmentActivity()?.let(::BiometricAuthenticator) }
    val texts = BiometricTexts(
        title = stringResource(R.string.enable_biometric_title),
        subtitle = stringResource(R.string.enable_biometric_subtitle),
        negativeButton = stringResource(R.string.cancel_action)
    )

    LaunchedEffect(authenticator) {
        viewModel.onBiometricAvailability(authenticator?.canUseStrong() == true)
    }
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            if (event is SecurityEvent.EnrollBiometric) biometricViewModel.enroll(event.pin)
        }
    }
    LaunchedEffect(biometricViewModel) {
        biometricViewModel.events.collect { event ->
            if (event is SecurityEvent.ShowBiometricPrompt) {
                authenticator?.authenticate(
                    texts = texts,
                    cipher = event.cipher,
                    onSuccess = { biometricViewModel.onPromptSucceeded() },
                    onFailure = { biometricViewModel.onPromptFailed() }
                ) ?: biometricViewModel.onPromptFailed()
            }
        }
    }
}

/** Итог операции — в снекбар; три потока операций делят один снекбар и показываются по очереди. */
@Composable
private fun MessageEffect(
    snackbarHostState: SnackbarHostState,
    message: SecurityMessage?,
    onShown: () -> Unit
) {
    val text = message?.let { stringResource(it.textRes) }
    LaunchedEffect(message) {
        if (text != null) {
            snackbarHostState.showSnackbar(text)
            onShown()
        }
    }
}

/** Предупреждения и ввод PIN при смене уровня защиты. */
@Composable
private fun LevelOverlays(success: SecurityUiState.Success, viewModel: SecurityViewModel) {
    success.dialog?.let { dialog ->
        SecurityDialogHost(
            dialog = dialog,
            isGuest = success.settings.isGuest,
            onConfirmLevel = viewModel::onLevelConfirmed,
            onAcceptBiometric = viewModel::onBiometricOfferAccepted,
            onDismiss = viewModel::onDismissed
        )
    }
    success.pinPrompt?.let { prompt ->
        PinPadDialog(
            prompt = prompt,
            onPinEntered = viewModel::onPinEntered,
            onDismiss = viewModel::onDismissed
        )
    }
}

/** Ввод PIN для вспомогательной операции (смена PIN, включение отпечатка). */
@Composable
private fun FlowPinPad(flow: SecurityFlowState, onPinEntered: (String) -> Unit, onDismiss: () -> Unit) {
    flow.pinPrompt?.let { prompt ->
        PinPadDialog(prompt = prompt, onPinEntered = onPinEntered, onDismiss = onDismiss)
    }
}

/**
 * Экран настроек без DI.
 *
 * @param busy Идёт операция — перезаворачивание ключа, досылка изменений, включение биометрии.
 */
@Composable
fun SecurityContent(
    uiState: SecurityUiState,
    busy: Boolean,
    actions: SecurityScreenActions,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        topBar = { TopBarSecurity(actions.onNavigateBack) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = modifier.padding(paddingValues)) {
            when (uiState) {
                is SecurityUiState.Error -> ErrorContent(onClick = {})
                is SecurityUiState.Loading -> LoadingProgressBar()
                is SecurityUiState.Success -> SecuritySettingsList(uiState, busy, actions)
            }
            if (busy) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun SecuritySettingsList(
    uiState: SecurityUiState.Success,
    busy: Boolean,
    actions: SecurityScreenActions
) {
    val settings = uiState.settings
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        SectionHeader(R.string.section_data_protection)
        ProtectionLevelSection(
            selected = settings.level,
            enabled = !busy,
            onSelect = actions.onLevelSelected
        )

        SectionHeader(R.string.section_pin)
        PinSection(
            hasPin = settings.hasPin,
            dataProtectedByPin = settings.level == DataProtectionLevel.PIN,
            actions = actions.pin
        )

        // Отпечаток выдаёт ключ данных только на уровне PIN: ниже ему нечего защищать
        if (settings.level == DataProtectionLevel.PIN && uiState.biometricAvailable) {
            SettingSwitch(
                title = R.string.biometric_unlock_title,
                description = R.string.biometric_unlock_description,
                checked = settings.biometricEnabled,
                onCheckedChange = actions.onBiometricToggled
            )
        }

        SectionHeader(R.string.section_screen)
        SettingSwitch(
            title = R.string.secure_screen_title,
            description = R.string.secure_screen_description,
            checked = settings.secureScreen,
            onCheckedChange = actions.onSecureScreenToggled
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun TopBarSecurity(onNavigateBack: () -> Unit) {
    TopBar(
        topBar = TopBarModel(
            title = R.string.security,
            navigationIcon = Icons.Filled.ArrowBack,
            navigationIconClick = { onNavigateBack() }
        )
    )
}
