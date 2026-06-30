package soft.divan.financemanager.core.auth.domain.provider

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.auth.domain.model.AuthEvent
import soft.divan.financemanager.core.auth.domain.model.AuthStatus
import soft.divan.financemanager.core.auth.domain.model.SessionState

/**
 * Провайдер состояния авторизации и контроллер переходов (Стейт-машина).
 *
 * Позволяет получать текущий статус сессии синхронно или реактивно,
 * а также управлять переходами между состояниями.
 */
interface AuthStateProvider {
    /**
     * Реактивный поток статуса авторизации — единый источник истины для UI и навигации.
     *
     * Эмитит значения только после восстановления состояния из хранилища, поэтому
     * не отдаёт промежуточный дефолт и согласован с тем, что видят сетевые интерцепторы.
     */
    fun observeStatus(): Flow<AuthStatus>

    /**
     * Возвращает текущий статус авторизации синхронно.
     */
    fun currentStatus(): AuthStatus

    /**
     * Возвращает атомарный снимок сессии (статус + токены).
     */
    fun currentSessionState(): SessionState

    /**
     * Отправляет событие для изменения состояния сессии.
     * Метод приостанавливаемый, чтобы гарантировать завершение всех побочных эффектов.
     */
    suspend fun sendEvent(event: AuthEvent)
}
