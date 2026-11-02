package soft.divan.financemanager.core.data.testing

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import soft.divan.financemanager.core.database.dao.AccountDao
import soft.divan.financemanager.core.database.dao.CategoryDao
import soft.divan.financemanager.core.database.dao.CurrencyDao
import soft.divan.financemanager.core.database.dao.OutboxDao
import soft.divan.financemanager.core.database.dao.TransactionDao
import soft.divan.financemanager.core.database.db.FinanceManagerDatabase
import soft.divan.financemanager.core.database.holder.DatabaseHolder
import soft.divan.financemanager.core.database.holder.DatabaseState

/**
 * Холдер, у которого база всегда открыта, — для тестов логики данных, а не жизненного цикла
 * базы (его проверяет `RoomDatabaseHolderTest` в `:core:database`).
 */
class OpenDatabaseHolder(private val database: FinanceManagerDatabase) : DatabaseHolder {

    override val state: StateFlow<DatabaseState> = MutableStateFlow(DatabaseState.OPEN)

    override suspend fun open(key: ByteArray) = Unit

    override suspend fun close() = Unit

    override suspend fun wipe() = Unit

    override suspend fun <T> withDatabase(block: suspend (FinanceManagerDatabase) -> T): T =
        block(database)

    override fun <T> observe(block: (FinanceManagerDatabase) -> Flow<T>): Flow<T> = block(database)
}

/** База-заглушка, отдающая переданные DAO, — для тестов источников данных с моками DAO. */
fun mockDatabase(
    accountDao: AccountDao = mockk(),
    categoryDao: CategoryDao = mockk(),
    currencyDao: CurrencyDao = mockk(),
    outboxDao: OutboxDao = mockk(),
    transactionDao: TransactionDao = mockk()
): FinanceManagerDatabase = mockk {
    every { accountDao() } returns accountDao
    every { categoryDao() } returns categoryDao
    every { currencyDao() } returns currencyDao
    every { outboxDao() } returns outboxDao
    every { transactionDao() } returns transactionDao
}
