package soft.divan.finansemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import soft.divan.finansemanager.core.database.entity.TransactionEntity

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions")
    suspend fun getAll(): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE isSynced = 0")
    suspend fun getPending(): List<TransactionEntity>

    @Query("UPDATE transactions SET isSynced = 1, remoteId = :remoteId WHERE localId = :localId")
    suspend fun markAsSynced(localId: String, remoteId: String)
}
