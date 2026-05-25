package soft.divan.financemanager.core.auth.data.authenticator

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import soft.divan.financemanager.core.auth.data.api.AuthApiService
import soft.divan.financemanager.core.auth.data.dto.RefreshRequestDto
import soft.divan.financemanager.core.auth.data.source.SessionLocalDataSource
import soft.divan.financemanager.core.auth.data.source.TokenLocalDataSource
import soft.divan.financemanager.core.auth.domain.model.AuthStatus
import soft.divan.financemanager.core.database.util.DatabaseCleanupManager
import java.io.IOException
import javax.inject.Inject
import javax.inject.Provider

/**
 * Менеджер авторизации, выступающий в роли единого источника истины (Single Source of Truth)
 * для управления JWT-токенами и состоянием сессии пользователя.
 *
 * Основные функции:
 * 1. **Кэширование**: Оптимизирует доступ к токенам через volatile-кэш в памяти, минимизируя обращения к диску.
 * 2. **Потокобезопасное обновление**: Использует [Mutex] для синхронизации параллельных запросов на обновление (refresh) токенов.
 *    Если несколько запросов одновременно получают HTTP 401, только первый инициирует сетевой вызов к API,
 *    остальные дожидаются результата и используют новый токен.
 * 3. **Селективная очистка**: При инвалидации сессии (протухший refresh-токен) автоматически удаляет
 *    пользовательские данные (транзакции, счета) через [DatabaseCleanupManager], сохраняя при этом
 *    системные данные (категории).
 * 4. **Поддержка гостевого режима**: Игнорирует попытки обновления для пользователей со статусом [AuthStatus.GUEST].
 *
 * @property tokenLocalDataSource Локальное хранилище для Access и Refresh токенов (DataStore).
 * @property sessionLocalDataSource Хранилище статуса авторизации ([AuthStatus]).
 * @property authApiProvider Ленивый провайдер API для запросов авторизации (используется Provider для избежания циклической зависимости).
 * @property databaseCleanupManager Менеджер для удаления пользовательских данных из БД.
 */

class AuthManager @Inject constructor(
    private val tokenLocalDataSource: TokenLocalDataSource,
    private val sessionLocalDataSource: SessionLocalDataSource,
    private val authApiProvider: Provider<AuthApiService>,
    private val databaseCleanupManager: DatabaseCleanupManager
) {
    // Mutex для синхронизации refresh-запросов
    private val mutex = Mutex()

    @Volatile
    private var cachedAccessToken: String? = null

    /**
     * Возвращает текущий access token.
     * Использует кэш в памяти для оптимизации доступа.
     *
     * @return Актуальный токен или null, если пользователь не авторизован.
     */
    suspend fun getAccessToken(): String? {
        // Проверяем статус, чтобы не возвращать токены для неавторизованной сессии
        val currentStatus = sessionLocalDataSource.getAuthStatus().first()
        if (currentStatus != AuthStatus.AUTHORIZED) return null

        return cachedAccessToken ?: tokenLocalDataSource.getAccessToken().first()
            ?.also { cachedAccessToken = it }
    }

    /**
     * Обновляет токены в локальном хранилище и кэше.
     */
    suspend fun updateTokens(accessToken: String?, refreshToken: String?) {
        tokenLocalDataSource.updateAccessToken(accessToken)
        tokenLocalDataSource.updateRefreshToken(refreshToken)
        cachedAccessToken = accessToken
    }

    /**
     * Очищает токены в локальном хранилище и кэше.
     */
    suspend fun clearTokens() {
        tokenLocalDataSource.clearTokens()
        cachedAccessToken = null
    }

    /**
     * Выполняет обновление access token через refresh token, если это необходимо.
     *
     * Алгоритм работы:
     * 1. Проверяет, не является ли пользователь гостем.
     * 2. Блокирует выполнение через [Mutex] для предотвращения гонки запросов.
     * 3. Сравнивает переданный [oldToken] с текущим. Если токен уже обновлен другим потоком, возвращает его.
     * 4. Выполняет сетевой запрос `/auth/refresh`.
     * 5. При успехе: обновляет локальное хранилище и возвращает новый токен.
     * 6. При ошибке авторизации (401/403): инициирует полную очистку сессии и данных.
     * 7. При сетевых сбоях ([IOException]): пробрасывает ошибку дальше, сохраняя сессию.
     *
     * @param oldToken Токен, с которым был отправлен упавший запрос (используется для дедупликации).
     * @return Новый access token или null, если обновление невозможно.
     * @throws IOException При ошибках соединения с сервером.
     */
    suspend fun refreshTokenIfNeeded(oldToken: String?): String? {
        mutex.withLock {
            // Если пользователь гость - не пытаемся обновлять токены и не сбрасываем сессию
            val currentStatus = sessionLocalDataSource.getAuthStatus().first()
            if (currentStatus == AuthStatus.GUEST) {
                return null
            }

            val currentToken = getAccessToken()

            // Если токен уже обновлён другим потоком — просто возвращаем актуальный токен
            if (!currentToken.isNullOrEmpty() && currentToken != oldToken) {
                return currentToken
            }

            val refreshToken = tokenLocalDataSource.getRefreshToken().first()

            if (refreshToken == null) {
                clearSession()
                return null
            }

            val response = try {
                val service = authApiProvider.get()
                service.refresh(RefreshRequestDto(refreshToken))
            } catch (e: IOException) {
                throw e
            } catch (e: Exception) {
                return null
            }

            if (response.isSuccessful && response.body() != null) {
                val newTokens = response.body()!!

                tokenLocalDataSource.updateAccessToken(newTokens.accessToken)
                tokenLocalDataSource.updateRefreshToken(newTokens.refreshToken)

                cachedAccessToken = newTokens.accessToken

                return newTokens.accessToken
            } else {
                // Если ошибка 401 или 403 — значит refresh token невалиден, очищаем сессию и данные
                if (response.code() == 401 || response.code() == 403) {
                    clearSession()
                }
                // Для остальных ошибок (например, 500) просто возвращаем null,
                // чтобы Authenticator вернул исходную 401 ошибку пользователю,
                // но не удалял токены из хранилища.
                return null
            }
        }
    }

    /**
     * Полная очистка состояния авторизации.
     * Стирает токены, устанавливает статус [AuthStatus.UNAUTHORIZED]
     * и инициирует селективное удаление пользовательских данных из БД.
     */
    private suspend fun clearSession() {
        cachedAccessToken = null
        tokenLocalDataSource.clearTokens()
        sessionLocalDataSource.clearSession()
        // Очищаем пользовательские данные при разлогине (401 ошибка)
        databaseCleanupManager.clearUserData()
    }
}
