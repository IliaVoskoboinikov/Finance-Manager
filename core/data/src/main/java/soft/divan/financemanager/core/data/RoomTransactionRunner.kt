package soft.divan.financemanager.core.data

import androidx.room.withTransaction
import soft.divan.financemanager.core.database.db.FinanceManagerDatabase
import soft.divan.financemanager.core.domain.result.DomainResult
import javax.inject.Inject

class RoomTransactionRunner @Inject constructor(
    private val db: FinanceManagerDatabase
) : TransactionRunner {

    override suspend fun <T> runInTransaction(block: suspend () -> T): T {
        return try {
            db.withTransaction {
                block()
            }
        } catch (e: TransactionRollbackException) {
            // Если мы сами выбросили это исключение, возвращаем Failure
            @Suppress("UNCHECKED_CAST")
            DomainResult.Failure(e.error) as T
        }
    }
}
