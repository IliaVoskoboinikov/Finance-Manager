package soft.divan.financemanager.core.auth.domain.model

/**
 * Атомарный снимок состояния сессии.
 * Включает в себя статус и связанные с ним данные (токены).
 */
sealed interface SessionState {
    val status: AuthStatus

    /** Состояние по умолчанию или после ошибки обновления токенов */
    data object Unauthorized : SessionState {
        override val status = AuthStatus.UNAUTHORIZED
    }

    /** Офлайн режим */
    data object Guest : SessionState {
        override val status = AuthStatus.GUEST
    }

    /** Активная сессия с валидными (предположительно) токенами */
    data class Authorized(
        val accessToken: String,
        val refreshToken: String
    ) : SessionState {
        override val status = AuthStatus.AUTHORIZED
    }
}

/**
 * События, которые могут изменить состояние системы.
 */
sealed interface AuthEvent {
    /** Успешный вход или регистрация */
    data class OnLoginSuccess(
        val accessToken: String,
        val refreshToken: String,
        val shouldMergeData: Boolean
    ) : AuthEvent

    /** Ручной выход */
    data class OnLogout(val shouldClearData: Boolean) : AuthEvent

    /** Токен протух и не может быть обновлен */
    data object OnSessionExpired : AuthEvent

    /** Переход в режим гостя */
    data object OnEnterAsGuest : AuthEvent

    /** Просто очистка данных без смены статуса */
    data object OnClearData : AuthEvent
}
