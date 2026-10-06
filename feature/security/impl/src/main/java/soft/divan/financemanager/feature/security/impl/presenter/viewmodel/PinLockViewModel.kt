package soft.divan.financemanager.feature.security.impl.presenter.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.auth.domain.model.AuthStatus
import soft.divan.financemanager.core.auth.domain.usecase.GetAuthStatusUseCase
import soft.divan.financemanager.core.data.vault.BiometricSession
import soft.divan.financemanager.core.data.vault.LocalDataBiometrics
import soft.divan.financemanager.core.domain.model.DataProtectionLevel
import soft.divan.financemanager.feature.security.impl.domain.model.PinCheckResult
import soft.divan.financemanager.feature.security.impl.domain.model.PinLockStatus
import soft.divan.financemanager.feature.security.impl.domain.usecase.ForgetPinUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.GetPinLockStatusUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.ObserveProtectionLevelUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.UnlockWithPinUseCase
import soft.divan.financemanager.feature.security.impl.presenter.model.PinLockError
import soft.divan.financemanager.feature.security.impl.presenter.model.PinLockEvent
import soft.divan.financemanager.feature.security.impl.presenter.model.PinLockUiState
import javax.inject.Inject

/**
 * Экран замка: и замок интерфейса, и разблокировка данных уровня PIN.
 *
 * Что именно проверяет PIN, решает [UnlockWithPinUseCase]; здесь — состояние экрана, биометрия и
 * «забыл PIN». Биометрия на уровне PIN идёт через шифр хранилища ключей: сессия живёт здесь, а
 * экран получает только шифр для `CryptoObject` и сообщает, чем закончился системный запрос.
 */
@HiltViewModel
class PinLockViewModel @Inject constructor(
    private val unlockWithPin: UnlockWithPinUseCase,
    private val getPinLockStatus: GetPinLockStatusUseCase,
    private val forgetPin: ForgetPinUseCase,
    private val observeProtectionLevel: ObserveProtectionLevelUseCase,
    private val biometrics: LocalDataBiometrics,
    private val getAuthStatus: GetAuthStatusUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PinLockUiState())
    val uiState: StateFlow<PinLockUiState> = _uiState.asStateFlow()

    private val _events = Channel<PinLockEvent>(Channel.BUFFERED)

    /** Одноразовые события: вход, стирание, запрос биометрии. */
    val events: Flow<PinLockEvent> = _events.receiveAsFlow()

    private var biometricSession: BiometricSession? = null

    /** Экран показан заново — сбрасывает прошлые ошибки и перечитывает состояние замка. */
    fun onShown() {
        // Запрос, брошенный прошлым показом (экран ушёл посреди запроса), больше не ждём
        val stale = biometricSession
        biometricSession = null
        viewModelScope.launch {
            stale?.let { biometrics.cancel(it) }
            // Замок показывается только при существующих ключах (данные открыты или заперты
            // PIN-ом), поэтому уровень известен сразу
            val pinLevel = observeProtectionLevel().first() == DataProtectionLevel.PIN
            _uiState.update {
                it.copy(
                    status = getPinLockStatus(),
                    error = null,
                    dataProtectedByPin = pinLevel,
                    biometricEnabled = !pinLevel || biometrics.observeEnabled().first(),
                    isGuest = getAuthStatus().first() != AuthStatus.AUTHORIZED,
                    inProgress = false
                )
            }
        }
    }

    /** Введён PIN целиком. */
    fun onPinEntered(pin: String) {
        if (_uiState.value.inProgress) return
        _uiState.update { it.copy(inProgress = true) }
        viewModelScope.launch {
            val result = unlockWithPin(pin)
            _uiState.update { it.copy(inProgress = false) }
            when (result) {
                PinCheckResult.Correct -> _events.send(PinLockEvent.Unlocked)

                PinCheckResult.Wiped -> _events.send(PinLockEvent.Wiped)

                is PinCheckResult.Wrong -> showStatus(
                    result.status,
                    PinLockError.WrongPin(result.status.attemptsLeft)
                )

                is PinCheckResult.LockedOut -> showStatus(result.status, PinLockError.LockedOut)

                // Экран восстановления покажет хост: состояние данных уже KeyLost
                PinCheckResult.KeyLost -> Unit
            }
        }
    }

    /** Пауза закончилась — ввод снова доступен. */
    fun onLockoutExpired() {
        _uiState.update { it.copy(status = it.status.copy(lockedUntil = null), error = null) }
    }

    /** Нажата кнопка биометрии или экран открылся — готовим системный запрос. */
    fun onBiometricRequested() {
        val state = _uiState.value
        if (!state.biometricEnabled || state.inProgress || biometricSession != null) return
        viewModelScope.launch {
            if (!state.dataProtectedByPin) {
                _events.send(PinLockEvent.ShowBiometricPrompt(cipher = null))
                return@launch
            }
            val session = biometrics.startUnlock()
            if (session == null) {
                _uiState.update {
                    it.copy(biometricEnabled = false, error = PinLockError.BiometricInvalidated)
                }
            } else {
                biometricSession = session
                _events.send(PinLockEvent.ShowBiometricPrompt(session.cipher))
            }
        }
    }

    /** Системный запрос биометрии завершился успешно. */
    fun onBiometricSucceeded() {
        val session = biometricSession
        biometricSession = null
        viewModelScope.launch {
            val unlocked = session == null || biometrics.finishUnlock(session)
            if (unlocked) {
                _events.send(PinLockEvent.Unlocked)
            } else {
                _uiState.update { it.copy(error = PinLockError.BiometricInvalidated) }
            }
        }
    }

    /** Запрос биометрии отменён или не удался — остаётся PIN. */
    fun onBiometricFailed() {
        val session = biometricSession ?: return
        biometricSession = null
        viewModelScope.launch { biometrics.cancel(session) }
    }

    /** «Забыл PIN» — показать предупреждение. */
    fun onForgotPinClicked() {
        _uiState.update { it.copy(showForgotPinDialog = true) }
    }

    /** Предупреждение закрыто без стирания. */
    fun onForgotPinDismissed() {
        _uiState.update { it.copy(showForgotPinDialog = false) }
    }

    /** Пользователь подтвердил стирание. */
    fun onForgotPinConfirmed() {
        _uiState.update { it.copy(showForgotPinDialog = false, inProgress = true) }
        viewModelScope.launch {
            forgetPin()
            _uiState.update { it.copy(inProgress = false) }
            _events.send(PinLockEvent.Wiped)
        }
    }

    private fun showStatus(status: PinLockStatus, error: PinLockError) {
        _uiState.update { it.copy(status = status, error = error) }
    }
}
