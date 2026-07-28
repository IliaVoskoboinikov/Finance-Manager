package soft.divan.financemanager.core.domain.repository

import soft.divan.financemanager.core.domain.result.DomainResult

interface AuthRepository {
    /**
     * Вход в аккаунт.
     * @param shouldMergeData если true, данные гостя будут привязаны к аккаунту.
     * Если false, локальные данные будут удалены перед входом.
     */
    suspend fun login(
        name: String,
        password: String,
        shouldMergeData: Boolean = false
    ): DomainResult<Unit>

    /**
     * Регистрация нового аккаунта.
     * @param shouldMergeData если true, данные гостя будут загружены в новый аккаунт.
     */
    suspend fun register(
        name: String,
        password: String,
        shouldMergeData: Boolean = false
    ): DomainResult<Unit>

    /**
     * Вход через Яндекс (OAuth).
     * @param accessToken access_token, полученный на устройстве от Яндекса.
     * Отправляется на бэкенд, который проверяет его и возвращает внутреннюю пару токенов.
     * @param shouldMergeData если true, данные гостя будут привязаны к аккаунту.
     */
    suspend fun loginWithYandex(
        accessToken: String,
        shouldMergeData: Boolean = false
    ): DomainResult<Unit>

    /**
     * Выход из аккаунта.
     * @param shouldClearData если true, будут удалены все пользовательские данные из БД (кроме категорий).
     */
    suspend fun logout(shouldClearData: Boolean): DomainResult<Unit>

    /**
     * Вход в режиме гостя.
     */
    suspend fun loginAsGuest(): DomainResult<Unit>

    /**
     * Очистка всех пользовательских данных (транзакции, счета и т.д.).
     */
    suspend fun clearUserData(): DomainResult<Unit>
}
