package soft.divan.financemanager.core.auth.data.provider

import android.util.Log
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import soft.divan.common.di.ApplicationScope
import soft.divan.financemanager.core.auth.data.source.SessionLocalDataSource
import soft.divan.financemanager.core.auth.data.source.TokenLocalDataSource
import soft.divan.financemanager.core.auth.domain.model.AuthEvent
import soft.divan.financemanager.core.auth.domain.model.AuthStatus
import soft.divan.financemanager.core.auth.domain.model.SessionState
import soft.divan.financemanager.core.auth.domain.provider.AuthStateProvider
import soft.divan.financemanager.core.database.util.DatabaseCleanupManager
import javax.inject.Inject

/**
 * Singleton-холдер состояния авторизации и центральный узел стейт-машины сессии.
 *
 * Класс реализует стратегию "Memory First":
 * 1. Любое изменение состояния сначала отражается в оперативной памяти ([_sessionState]).
 * 2. Это гарантирует мгновенную и консистентную реакцию UI и сетевых интерцепторов.
 * 3. Синхронизация с постоянным хранилищем (DataStore, KeyStore) и БД выполняется как побочный эффект.
 *
 * Потокобезопасность обеспечивается использованием [Mutex], что гарантирует
 * строгую последовательность выполнения переходов и исключает Race Conditions.
 */
class AuthStateHolder @Inject constructor(
    private val sessionDataSource: SessionLocalDataSource,
    private val tokenDataSource: TokenLocalDataSource,
    private val dbCleanupManager: DatabaseCleanupManager,
    @param:ApplicationScope private val scope: CoroutineScope
) : AuthStateProvider {

    private val mutex = Mutex()

    /**
     * Внутреннее состояние сессии. Является единственным источником истины для приложения.
     */
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Unauthorized)

    /**
     * Сигнал завершения первичного восстановления состояния из хранилища.
     * Пока он не завершён, [_sessionState] содержит дефолт [SessionState.Unauthorized],
     * который не отражает реальную сессию пользователя.
     */
    private val initialization = CompletableDeferred<Unit>()

    init {
        // Восстановление состояния из персистентного хранилища при холодном старте.
        scope.launch {
            try {
                val status = sessionDataSource.getAuthStatus().first()
                val access = tokenDataSource.getAccessToken().first()
                val refresh = tokenDataSource.getRefreshToken().first()

                _sessionState.value = mapToSessionState(status, access, refresh)
            } finally {
                initialization.complete(Unit)
            }
        }
    }

    /**
     * Гарантирует, что состояние восстановлено перед синхронным чтением.
     *
     * Вызывается только из сетевых интерцепторов ([currentStatus]/[currentSessionState]),
     * которые работают на фоновых потоках OkHttp, поэтому короткая блокировка здесь безопасна
     * и исключает гонку, когда запрос уходит до восстановления сессии (без токена/со статусом
     * UNAUTHORIZED, что приводило бы к ложной блокировке запроса авторизованного пользователя).
     */
    private fun awaitRestore() {
        if (!initialization.isCompleted) {
            runBlocking { initialization.await() }
        }
    }

    override fun currentStatus(): AuthStatus {
        awaitRestore()
        return _sessionState.value.status
    }

    override fun currentSessionState(): SessionState {
        awaitRestore()
        return _sessionState.value
    }

    /**
     * Реактивный статус из in-memory состояния (единый источник истины).
     * Сначала дожидается восстановления, затем транслирует изменения [_sessionState],
     * чтобы UI/навигация не реагировали на промежуточный дефолт при холодном старте.
     */
    override fun observeStatus(): Flow<AuthStatus> = flow {
        initialization.await()
        emitAll(_sessionState.map { it.status }.distinctUntilChanged())
    }

    /**
     * Основная точка входа для изменения состояния сессии.
     * Метод является приостанавливаемым (suspend), чтобы гарантировать завершение
     * всех критических побочных эффектов (например, очистки БД) перед возвратом управления.
     *
     * @param event Событие, инициирующее переход.
     */
    override suspend fun sendEvent(event: AuthEvent) {
        mutex.withLock {
            when (event) {
                is AuthEvent.OnLoginSuccess -> handleLogin(event)
                is AuthEvent.OnLogout -> handleLogout(event)
                is AuthEvent.OnSessionExpired -> handleSessionExpired()
                is AuthEvent.OnEnterAsGuest -> handleEnterAsGuest()
                is AuthEvent.OnClearData -> dbCleanupManager.clearUserData()
            }
            Log.i("AuthState", "Transition finished: -> ${_sessionState.value::class.simpleName}")
        }
    }

    /**
     * Логика успешного входа: очистка старых данных (если нужно), сохранение новых токенов
     * и мгновенная активация сессии в памяти.
     */
    private suspend fun handleLogin(event: AuthEvent.OnLoginSuccess) {
        // Читаем состояние напрямую: метод уже выполняется внутри mutex на скоупе приложения,
        // а awaitRestore()/currentStatus() предназначены только для сетевых (фоновых) потоков.
        val previousStatus = _sessionState.value.status

        if (previousStatus == AuthStatus.GUEST && !event.shouldMergeData) {
            Log.i("AuthState", "Clearing database as requested (Login with data wipe)")
            dbCleanupManager.clearUserData()
        }

        // RAM update first
        _sessionState.value = SessionState.Authorized(event.accessToken, event.refreshToken)

        // Disk persistence in background
        tokenDataSource.updateAccessToken(event.accessToken)
        tokenDataSource.updateRefreshToken(event.refreshToken)
        sessionDataSource.setAuthStatus(AuthStatus.AUTHORIZED)
    }

    /**
     * Логика выхода. Если [AuthEvent.OnLogout.shouldClearData] истинно, сессия
     * сбрасывается в [SessionState.Unauthorized], иначе пользователь переходит в режим [SessionState.Guest].
     */
    private suspend fun handleLogout(event: AuthEvent.OnLogout) {
        if (event.shouldClearData) {
            _sessionState.value = SessionState.Unauthorized
            tokenDataSource.clearTokens()
            dbCleanupManager.clearUserData()
            sessionDataSource.setAuthStatus(AuthStatus.UNAUTHORIZED)
        } else {
            _sessionState.value = SessionState.Guest
            tokenDataSource.clearTokens()
            sessionDataSource.setAuthStatus(AuthStatus.GUEST)
        }
    }

    /**
     * Экстренный сценарий инвалидации сессии (ошибка 401 при обновлении токена).
     * Выполняет тотальную очистку данных для защиты приватности.
     */
    private suspend fun handleSessionExpired() {
        _sessionState.value = SessionState.Unauthorized
        tokenDataSource.clearTokens()
        dbCleanupManager.clearUserData()
        sessionDataSource.setAuthStatus(AuthStatus.UNAUTHORIZED)
    }

    /**
     * Переход в автономный режим. Стирает токены, но сохраняет локальные данные.
     */
    private suspend fun handleEnterAsGuest() {
        _sessionState.value = SessionState.Guest
        tokenDataSource.clearTokens()
        sessionDataSource.setAuthStatus(AuthStatus.GUEST)
    }

    /**
     * Маппинг сырых данных из хранилищ в атомарный объект [SessionState].
     */
    private fun mapToSessionState(status: AuthStatus, access: String?, refresh: String?): SessionState {
        return when (status) {
            AuthStatus.GUEST -> SessionState.Guest

            AuthStatus.UNAUTHORIZED -> SessionState.Unauthorized

            AuthStatus.AUTHORIZED -> {
                if (access != null && refresh != null) {
                    SessionState.Authorized(access, refresh)
                } else {
                    SessionState.Unauthorized
                }
            }
        }
    }
}
