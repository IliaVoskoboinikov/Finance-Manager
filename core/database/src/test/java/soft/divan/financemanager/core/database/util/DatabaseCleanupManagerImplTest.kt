package soft.divan.financemanager.core.database.util

import io.mockk.Called
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import soft.divan.financemanager.core.database.dao.AccountDao
import soft.divan.financemanager.core.database.dao.CategoryDao
import soft.divan.financemanager.core.database.dao.TransactionDao

class DatabaseCleanupManagerImplTest {

    private val accountDao = mockk<AccountDao>(relaxUnitFun = true)
    private val transactionDao = mockk<TransactionDao>(relaxUnitFun = true)
    private val categoryDao = mockk<CategoryDao>(relaxUnitFun = true)

    private val cleanupManager = DatabaseCleanupManagerImpl(
        accountDao = accountDao,
        transactionDao = transactionDao
    )

    @Test
    fun `clearUserData removes transactions and accounts`() = runTest {
        cleanupManager.clearUserData()

        coVerify(exactly = 1) { transactionDao.deleteAll() }
        coVerify(exactly = 1) { accountDao.deleteAll() }
    }

    @Test
    fun `clearUserData removes transactions before accounts`() = runTest {
        cleanupManager.clearUserData()

        coVerifyOrder {
            transactionDao.deleteAll()
            accountDao.deleteAll()
        }
    }

    @Test
    fun `clearUserData never touches categories`() = runTest {
        cleanupManager.clearUserData()

        coVerify { categoryDao wasNot Called }
    }
}
