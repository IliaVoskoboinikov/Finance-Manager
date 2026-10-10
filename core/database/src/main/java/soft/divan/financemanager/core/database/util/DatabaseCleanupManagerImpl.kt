package soft.divan.financemanager.core.database.util

import soft.divan.financemanager.core.database.dao.AccountDao
import soft.divan.financemanager.core.database.dao.OutboxDao
import soft.divan.financemanager.core.database.dao.TransactionDao
import javax.inject.Inject

class DatabaseCleanupManagerImpl @Inject constructor(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao,
    private val outboxDao: OutboxDao
) : DatabaseCleanupManager {

    override suspend fun clearUserData() {
        // Очередь чистим первой: её записи ссылаются на удаляемые данные, а отправлять операции
        // ушедшего пользователя под новой сессией нельзя.
        outboxDao.deleteAll()
        // Удаляем транзакции и счета, но не трогаем категории
        transactionDao.deleteAll()
        accountDao.deleteAll()
    }
}
