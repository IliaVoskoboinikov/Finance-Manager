package soft.divan.financemanager.feature.auth.impl.presenter.screen

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yandex.authsdk.YandexAuthLoginOptions
import soft.divan.financemanager.feature.auth.impl.R
import soft.divan.financemanager.feature.auth.impl.presenter.model.AuthAction
import soft.divan.financemanager.feature.auth.impl.presenter.model.AuthActions
import soft.divan.financemanager.feature.auth.impl.presenter.model.AuthEvent
import soft.divan.financemanager.feature.auth.impl.presenter.model.AuthUi
import soft.divan.financemanager.feature.auth.impl.presenter.model.AuthUiState
import soft.divan.financemanager.feature.auth.impl.presenter.screen.components.MergeDialog
import soft.divan.financemanager.feature.auth.impl.presenter.viewModel.AuthViewModel
import soft.divan.financemanager.uikit.components.ErrorContent
import soft.divan.financemanager.uikit.components.LoadingProgressBar
import soft.divan.financemanager.uikit.components.TopBar
import soft.divan.financemanager.uikit.model.TopBarModel
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme
import soft.divan.financemanager.uikit.theme.YandexRed

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val yandexLauncher = rememberLauncherForActivityResult(viewModel.yandexAuthContract) { result ->
        viewModel.onYandexResult(result)
    }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is AuthEvent.NavigateToMain -> onAuthSuccess()

                is AuthEvent.ShowToast -> Toast.makeText(
                    context,
                    event.message,
                    Toast.LENGTH_SHORT
                ).show()

                is AuthEvent.ShowError -> {
                    /* handle error */
                }
            }
        }
    }

    AuthContent(
        uiState = uiState,
        actions = AuthActions(
            onUpdateName = { viewModel.onAction(AuthAction.UpdateName(it)) },
            onUpdatePassword = { viewModel.onAction(AuthAction.UpdatePassword(it)) },
            onToggleMode = { viewModel.onAction(AuthAction.ToggleMode) },
            onAuthClick = { viewModel.onAction(AuthAction.OnAuthClick) },
            onMergeConfirm = { viewModel.onAction(AuthAction.OnMergeConfirm(it)) },
            onLogoutClick = { viewModel.onAction(AuthAction.OnLogoutClick) },
            onLogoutConfirm = { viewModel.onAction(AuthAction.OnLogoutConfirm(it)) },
            onGuestClick = { viewModel.onAction(AuthAction.OnGuestClick) },
            onYandexClick = { yandexLauncher.launch(YandexAuthLoginOptions()) },
            onDismissDialogs = { viewModel.onAction(AuthAction.DismissDialogs) }
        ),
        snackbarHostState = snackbarHostState
    )
}

@Composable
fun AuthContent(
    uiState: AuthUiState,
    actions: AuthActions,
    snackbarHostState: SnackbarHostState
) {
    if (uiState is AuthUiState.Success && uiState.showMergeDialog) {
        MergeDialog(
            onConfirm = actions.onMergeConfirm,
            onDismiss = actions.onDismissDialogs
        )
    }

    Scaffold(
        topBar = {
            if (uiState is AuthUiState.Success) {
                TopBar(
                    topBar = TopBarModel(
                        title = if (uiState.authUi.isLoginMode) {
                            R.string.auth_sign_in_button
                        } else {
                            R.string.auth_sign_up_button
                        },
                        navigationIcon = null,
                        navigationIconClick = {}
                    )
                )
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is AuthUiState.Loading -> LoadingProgressBar()

                is AuthUiState.Error -> ErrorContent(onClick = actions.onAuthClick)

                is AuthUiState.Success -> SuccessContent(
                    content = uiState,
                    actions = actions
                )
            }
        }
    }
}

@Composable
private fun SuccessContent(
    content: AuthUiState.Success,
    actions: AuthActions
) {
    val authUi = content.authUi

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AuthHeader(isLoginMode = authUi.isLoginMode)

                AuthCard(
                    authUi = authUi,
                    errorMessage = content.errorMessage,
                    actions = actions
                )

                AlternativeAuthButtons(
                    onYandexClick = actions.onYandexClick,
                    onGuestClick = actions.onGuestClick
                )
            }

            if (content.isSyncing) {
                SyncingOverlay()
            }
        }
    }
}

@Composable
private fun AuthCard(
    authUi: AuthUi,
    errorMessage: Int?,
    actions: AuthActions
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AuthFields(
                authUi = authUi,
                error = errorMessage?.let { stringResource(it) },
                onUpdateName = actions.onUpdateName,
                onUpdatePassword = actions.onUpdatePassword
            )

            Spacer(modifier = Modifier.height(8.dp))

            AuthButtons(
                isLoginMode = authUi.isLoginMode,
                isInputValid = authUi.name.isNotBlank() && authUi.pass.isNotBlank(),
                onAuthClick = actions.onAuthClick,
                onToggleMode = actions.onToggleMode
            )
        }
    }
}

@Composable
private fun AuthHeader(isLoginMode: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(48.dp),
                imageVector = Icons.Default.Wallet,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = if (isLoginMode) {
                stringResource(
                    R.string.auth_welcome_back
                )
            } else {
                stringResource(R.string.auth_create_account)
            },
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text(
            text = if (isLoginMode) {
                stringResource(
                    R.string.auth_sign_in_continue
                )
            } else {
                stringResource(R.string.auth_sign_up_start)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AlternativeAuthButtons(
    onYandexClick: () -> Unit,
    onGuestClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Text(
                text = stringResource(R.string.auth_or_divider),
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onYandexClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = YandexRed,
                contentColor = Color.White
            )
        ) {
            Text(
                text = stringResource(R.string.auth_sign_in_yandex),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onGuestClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = stringResource(R.string.auth_continue_as_guest))
        }
    }
}

@Composable
private fun AuthFields(
    authUi: AuthUi,
    error: String?,
    onUpdateName: (String) -> Unit,
    onUpdatePassword: (String) -> Unit
) {
    OutlinedTextField(
        value = authUi.name,
        onValueChange = onUpdateName,
        label = { Text(stringResource(R.string.auth_name_label)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )

    Spacer(modifier = Modifier.height(16.dp))

    OutlinedTextField(
        value = authUi.pass,
        onValueChange = onUpdatePassword,
        label = { Text(stringResource(R.string.auth_password_label)) },
        modifier = Modifier.fillMaxWidth(),
        visualTransformation = PasswordVisualTransformation(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        )
    )

    if (error != null) {
        Text(
            text = error,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, start = 4.dp)
        )
    }
}

@Composable
private fun AuthButtons(
    isLoginMode: Boolean,
    isInputValid: Boolean,
    onAuthClick: () -> Unit,
    onToggleMode: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onAuthClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = isInputValid,
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = if (isLoginMode) {
                    stringResource(
                        R.string.auth_sign_in_button
                    )
                } else {
                    stringResource(R.string.auth_sign_up_button)
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        TextButton(
            onClick = onToggleMode,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isLoginMode) {
                    stringResource(
                        R.string.auth_no_account_register
                    )
                } else {
                    stringResource(R.string.auth_have_account_login)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun SyncingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = stringResource(R.string.auth_syncing_overlay))
            }
        }
    }
}

@Preview(showBackground = true, name = "Login Mode - Light")
@Composable
fun AuthScreenLoginLightPreview() {
    FinanceManagerTheme(darkTheme = false) {
        AuthContent(
            uiState = AuthUiState.Success(
                authUi = AuthUi(
                    name = "John Doe",
                    pass = "password123",
                    isLoginMode = true
                )
            ),
            actions = AuthActions(),
            snackbarHostState = SnackbarHostState()
        )
    }
}

@Preview(showBackground = true, name = "Login Mode - Dark")
@Composable
fun AuthScreenLoginDarkPreview() {
    FinanceManagerTheme(darkTheme = true) {
        AuthContent(
            uiState = AuthUiState.Success(
                authUi = AuthUi(
                    name = "John Doe",
                    pass = "password123",
                    isLoginMode = true
                )
            ),
            actions = AuthActions(),
            snackbarHostState = SnackbarHostState()
        )
    }
}

@Preview(showBackground = true, name = "Register Mode")
@Composable
fun AuthScreenRegisterPreview() {
    FinanceManagerTheme {
        AuthContent(
            uiState = AuthUiState.Success(
                authUi = AuthUi(
                    name = "New User",
                    pass = "securePass",
                    isLoginMode = false
                )
            ),
            actions = AuthActions(),
            snackbarHostState = SnackbarHostState()
        )
    }
}

@Preview(showBackground = true, name = "Error State")
@Composable
fun AuthScreenErrorPreview() {
    FinanceManagerTheme {
        AuthContent(
            uiState = AuthUiState.Success(
                authUi = AuthUi(
                    name = "John Doe",
                    pass = "wrong",
                    isLoginMode = true
                ),
                errorMessage = R.string.auth_error_failed
            ),
            actions = AuthActions(),
            snackbarHostState = SnackbarHostState()
        )
    }
}

@Preview(showBackground = true, name = "Russian Localization", locale = "ru")
@Composable
fun AuthScreenRussianPreview() {
    FinanceManagerTheme {
        AuthContent(
            uiState = AuthUiState.Success(
                authUi = AuthUi(
                    name = "Иван Иванов",
                    pass = "пароль123",
                    isLoginMode = true
                )
            ),
            actions = AuthActions(),
            snackbarHostState = SnackbarHostState()
        )
    }
}
