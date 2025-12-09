package soft.divan.finansemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import soft.divan.finansemanager.core.database.entity.TransactionEntity

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions")
    suspend fun getAll(): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<TransactionEntity>)

    @Query(
        """
    SELECT * FROM transactions
    WHERE accountIdLocal = :accountId
      AND date(transactionDate) BETWEEN date(:startDate) AND date(:endDate)
    ORDER BY transactionDate ASC
"""
    )
    fun getTransactionsByAccountAndPeriod(
        accountId: String,
        startDate: String, // "2025-10-24"
        endDate: String    // "2025-10-24"
    ): Flow<List<TransactionEntity>>

    @Query(
        """
        UPDATE transactions
        SET id = :newId, isSynced = 1
        WHERE createdAt = :createdAt
    """
    )
    suspend fun updateTransactionId(createdAt: String, newId: Int)

    @Query("DELETE FROM transactions WHERE id = :transactionId")
    suspend fun deleteTransaction(transactionId: Int)

    @Query("SELECT * FROM transactions WHERE id = :transactionId LIMIT 1")
    suspend fun getTransactionById(transactionId: Int): TransactionEntity?

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE accountIdLocal = :accountId ORDER BY transactionDate DESC")
    suspend fun getByAccountId(accountId: String): List<TransactionEntity>
}
