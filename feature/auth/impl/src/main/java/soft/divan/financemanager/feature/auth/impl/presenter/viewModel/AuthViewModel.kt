package soft.divan.financemanager.feature.auth.impl.presenter.viewModel

import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yandex.authsdk.YandexAuthLoginOptions
import com.yandex.authsdk.YandexAuthResult
import com.yandex.authsdk.YandexAuthSdk
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.auth.domain.model.AuthStatus
import soft.divan.financemanager.core.auth.domain.usecase.GetAuthStatusUseCase
import soft.divan.financemanager.core.domain.error.DomainError
import soft.divan.financemanager.core.domain.repository.AuthRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.domain.result.fold
import soft.divan.financemanager.core.loggingerror.ErrorLogger
import soft.divan.financemanager.feature.auth.impl.R
import soft.divan.financemanager.feature.auth.impl.presenter.model.AuthAction
import soft.divan.financemanager.feature.auth.impl.presenter.model.AuthEvent
import soft.divan.financemanager.feature.auth.impl.presenter.model.AuthUiState
import soft.divan.financemanager.sync.worker.SyncCoordinator
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val getAuthStatusUseCase: GetAuthStatusUseCase,
    private val syncCoordinator: SyncCoordinator,
    private val yandexAuthSdk: YandexAuthSdk,
    private val errorLogger: ErrorLogger
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Success())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<AuthEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val authStatus = getAuthStatusUseCase()

    /** Идущий вход гостем. */
    private var guestJob: Job? = null

    /**
     * Контракт Yandex ID SDK для запуска экрана входа через `rememberLauncherForActivityResult`.
     * Результат передаётся обратно в [onYandexResult].
     */
    val yandexAuthContract: ActivityResultContract<YandexAuthLoginOptions, YandexAuthResult>
        get() = yandexAuthSdk.contract

    fun onAction(action: AuthAction) {
        when (action) {
            is AuthAction.UpdateName -> {
                _uiState.updateContent { it.copy(authUi = it.authUi.copy(name = action.name)) }
            }

            is AuthAction.UpdatePassword -> {
                _uiState.updateContent { it.copy(authUi = it.authUi.copy(pass = action.pass)) }
            }

            AuthAction.ToggleMode -> {
                _uiState.updateContent {
                    it.copy(
                        authUi = it.authUi.copy(isLoginMode = !it.authUi.isLoginMode)
                    )
                }
            }

            AuthAction.OnAuthClick -> performAuth()

            is AuthAction.OnMergeConfirm -> onMergeConfirm(action.shouldMerge)

            AuthAction.OnLogoutClick -> {
                _uiState.updateContent { it.copy(showLogoutDialog = true) }
            }

            is AuthAction.OnLogoutConfirm -> logout(action.shouldClear)

            AuthAction.OnGuestClick -> signInAsGuest()

            AuthAction.DismissDialogs -> {
                _uiState.updateContent { it.copy(showMergeDialog = false, showLogoutDialog = false) }
            }
        }
    }

    /**
     * Форма, готовая принять новый вход. Во время первичного синка её кнопки остаются под
     * оверлеем и нажимаются: второй вход посреди синка сменил бы сессию, которую тот синкает.
     * То же во время входа гостем: исход двух входов подряд зависел бы от порядка событий.
     */
    private val idleContent: AuthUiState.Success?
        get() = (_uiState.value as? AuthUiState.Success)?.takeUnless {
            it.isSyncing || guestJob?.isActive == true
        }

    private fun performAuth() {
        val content = idleContent ?: return
        val name = content.authUi.name
        val pass = content.authUi.pass
        val isLogin = content.authUi.isLoginMode

        runAuth(content) {
            if (isLogin) {
                authRepository.login(name, pass, shouldMergeData = true)
            } else {
                authRepository.register(name, pass, shouldMergeData = true)
            }
        }
    }

    /**
     * Обрабатывает результат экрана входа Yandex ID SDK.
     * При успехе обменивает access_token Яндекса на внутреннюю пару токенов,
     * при ошибке показывает сообщение, при отмене — ничего не делает.
     */
    fun onYandexResult(result: YandexAuthResult) {
        when (result) {
            is YandexAuthResult.Success -> {
                val content = idleContent ?: return
                runAuth(content) {
                    authRepository.loginWithYandex(result.token.value, shouldMergeData = true)
                }
            }

            is YandexAuthResult.Failure -> {
                // Фиксируем причину сбоя SDK (коды ошибок, без токенов/PII),
                // иначе ошибка Яндекса теряется и диагностировать нечего.
                errorLogger.recordError(result.exception, "Yandex auth failed")
                _uiState.updateContent {
                    it.copy(errorMessage = R.string.auth_error_yandex)
                }
            }

            YandexAuthResult.Cancelled -> Unit
        }
    }

    /**
     * Общий сценарий входа: показывает загрузку, при успехе решает вопрос слияния
     * гостевых данных / запускает первичную синхронизацию, при ошибке — показывает сообщение.
     * Используется обычным логином/регистрацией и входом через Яндекс.
     */
    private fun runAuth(
        previousContent: AuthUiState.Success,
        authCall: suspend () -> DomainResult<Unit>
    ) {
        _uiState.value = AuthUiState.Loading

        viewModelScope.launch {
            val wasGuest = getAuthStatusUseCase().first() == AuthStatus.GUEST

            authCall().fold(
                onSuccess = {
                    if (wasGuest) {
                        _uiState.value = previousContent.copy(showMergeDialog = true)
                    } else {
                        performInitialSync(previousContent)
                    }
                },
                onFailure = { error ->
                    _uiState.value = previousContent.copy(errorMessage = error.toMessageRes())
                }
            )
        }
    }

    /**
     * Маппинг доменной ошибки в конкретное сообщение, чтобы пользователь видел причину
     * (неверные данные / нет сети), а не один обобщённый текст на все случаи.
     */
    @StringRes
    private fun DomainError.toMessageRes(): Int = when (this) {
        is DomainError.Unauthorized -> R.string.auth_error_invalid_credentials
        is DomainError.NetworkUnavailable -> R.string.auth_error_network
        else -> R.string.auth_error_failed
    }

    private fun performInitialSync(successState: AuthUiState.Success) {
        _uiState.value = successState.copy(isSyncing = true)
        viewModelScope.launch {
            val isSyncSuccess = syncCoordinator.syncAll()
            if (!isSyncSuccess) {
                _eventFlow.emit(AuthEvent.ShowToast(R.string.auth_sync_failed))
            }
            _eventFlow.emit(AuthEvent.NavigateToMain)
            _uiState.value = successState.copy(isSyncing = false)
        }
    }

    private fun onMergeConfirm(shouldMerge: Boolean) {
        val content = _uiState.value as? AuthUiState.Success ?: return
        // Ответ принимается только пока диалог открыт: второй тап по его кнопке иначе ещё раз
        // очистил бы данные и запустил второй синк.
        if (!content.showMergeDialog) return
        // Синк считается начатым сразу, а не после очистки данных: иначе в этом окне форма
        // принимает новый вход.
        val dialogClosed = content.copy(showMergeDialog = false, isSyncing = true)
        _uiState.value = dialogClosed
        viewModelScope.launch {
            if (!shouldMerge) {
                authRepository.clearUserData()
            }
            performInitialSync(dialogClosed)
        }
    }

    private fun logout(shouldClear: Boolean) {
        val content = _uiState.value as? AuthUiState.Success ?: return
        _uiState.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.logout(shouldClear).fold(
                onSuccess = { _uiState.value = AuthUiState.Success() },
                onFailure = { _uiState.value = content }
            )
        }
    }

    private fun signInAsGuest() {
        // Повторный тап, пока вход идёт, игнорируется. После успеха повтор разрешён намеренно:
        // вход гостем и переход на главный идемпотентны, а событие перехода может потеряться
        // (SharedFlow без подписчика при пересоздании экрана) — тогда повторный тап спасает.
        if (idleContent == null) return
        guestJob = viewModelScope.launch {
            authRepository.loginAsGuest().fold(
                onSuccess = { _eventFlow.emit(AuthEvent.NavigateToMain) },
                onFailure = { /* ignore */ }
            )
        }
    }

    private fun MutableStateFlow<AuthUiState>.updateContent(
        update: (AuthUiState.Success) -> AuthUiState.Success
    ) {
        this.update { currentState ->
            if (currentState is AuthUiState.Success) update(currentState) else currentState
        }
    }
}
