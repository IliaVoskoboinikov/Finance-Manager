package soft.divan.financemanager.core.database.util

import soft.divan.financemanager.core.database.dao.AccountDao
import soft.divan.financemanager.core.database.dao.TransactionDao
import javax.inject.Inject

class DatabaseCleanupManagerImpl @Inject constructor(
    private val accountDao: AccountDao,
    private val transactionDao: TransactionDao
) : DatabaseCleanupManager {

    override suspend fun clearUserData() {
        // Удаляем транзакции и счета, но не трогаем категории
        transactionDao.deleteAll()
        accountDao.deleteAll()
    }
}
