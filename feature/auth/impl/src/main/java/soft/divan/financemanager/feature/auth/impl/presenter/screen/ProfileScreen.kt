package soft.divan.financemanager.feature.auth.impl.presenter.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import soft.divan.financemanager.core.auth.domain.model.AuthStatus
import soft.divan.financemanager.feature.auth.impl.presenter.model.AuthAction
import soft.divan.financemanager.feature.auth.impl.presenter.model.AuthUiState
import soft.divan.financemanager.feature.auth.impl.presenter.viewModel.AuthViewModel

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import soft.divan.financemanager.feature.auth.impl.R
import soft.divan.financemanager.feature.auth.impl.presenter.model.ProfileActions
import soft.divan.financemanager.feature.auth.impl.presenter.screen.components.LogoutDialog
import soft.divan.financemanager.uikit.components.ErrorContent
import soft.divan.financemanager.uikit.components.LoadingProgressBar
import soft.divan.financemanager.uikit.components.TopBar
import soft.divan.financemanager.uikit.model.TopBarModel

@Composable
fun ProfileScreen(
    onNavigateToAuth: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authStatus by viewModel.authStatus.collectAsStateWithLifecycle(
        initialValue = AuthStatus.UNAUTHORIZED
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileContent(
        uiState = uiState,
        authStatus = authStatus,
        actions = ProfileActions(
            onNavigateToAuth = onNavigateToAuth,
            onLogoutClick = { viewModel.onAction(AuthAction.OnLogoutClick) },
            onLogoutConfirm = { viewModel.onAction(AuthAction.OnLogoutConfirm(it)) },
            onDismissDialogs = { viewModel.onAction(AuthAction.DismissDialogs) }
        )
    )
}

@Composable
fun ProfileContent(
    uiState: AuthUiState,
    authStatus: AuthStatus,
    actions: ProfileActions
) {
    Scaffold(
        topBar = {
            TopBar(
                topBar = TopBarModel(
                    title = R.string.profile_title,
                    navigationIcon = null,
                    navigationIconClick = {}
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (uiState) {
                is AuthUiState.Loading -> LoadingProgressBar()

                is AuthUiState.Error -> ErrorContent(onClick = { /* retry? */ })

                is AuthUiState.Success -> ProfileSuccessContent(
                    content = uiState,
                    authStatus = authStatus,
                    actions = actions
                )
            }
        }
    }
}

@Composable
private fun ProfileSuccessContent(
    content: AuthUiState.Success,
    authStatus: AuthStatus,
    actions: ProfileActions
) {
    if (content.showLogoutDialog) {
        LogoutDialog(onConfirm = actions.onLogoutConfirm, onDismiss = actions.onDismissDialogs)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ProfileInfo(authStatus = authStatus, actions = actions)
    }
}

@Composable
private fun ProfileInfo(authStatus: AuthStatus, actions: ProfileActions) {
    when (authStatus) {
        AuthStatus.GUEST -> {
            Text(text = stringResource(R.string.profile_guest_message))
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = actions.onNavigateToAuth,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.profile_sign_in_register))
            }
        }

        AuthStatus.AUTHORIZED -> {
            Text(text = stringResource(R.string.profile_authorized_message))
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = actions.onLogoutClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.profile_logout))
            }
        }

        else -> {
            Button(
                onClick = actions.onNavigateToAuth,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.profile_sign_in_register))
            }
        }
    }
}
