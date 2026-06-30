package soft.divan.financemanager.core.database.util

interface DatabaseCleanupManager {
    /**
     * Удаляет все пользовательские данные (счета, транзакции),
     * но сохраняет системные данные (категории).
     */
    suspend fun clearUserData()
}
