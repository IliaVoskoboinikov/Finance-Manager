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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Query(
        """
    SELECT * FROM transactions
    WHERE accountLocalId = :accountId
      AND date(transactionDate) BETWEEN date(:startDate) AND date(:endDate)
    ORDER BY transactionDate ASC
"""
    )
    fun getByAccountAndPeriod(
        accountId: String,
        startDate: String, // "2025-10-24"
        endDate: String    // "2025-10-24"
    ): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE localId = :localId")
    suspend fun getByLocalId(localId: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE serverId = :serverId")
    suspend fun getByServerId(serverId: Int): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE serverId IN (:serverIds)")
    suspend fun getByServerIds(serverIds: List<Int>): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE accountLocalId = :accountId ORDER BY transactionDate DESC")
    suspend fun getByAccountId(accountId: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingSync(): List<TransactionEntity>

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE localId = :localId")
    suspend fun delete(localId: String)
}
