package soft.divan.finansemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import soft.divan.finansemanager.core.database.entity.AccountEntity
import soft.divan.finansemanager.core.database.model.SyncStatus

@Dao
interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccounts(accounts: List<AccountEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: AccountEntity)

    @Query("SELECT * FROM account")
    fun getAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM account WHERE localId = :id")
    suspend fun getById(id: String): AccountEntity?

    @Query("SELECT * FROM account WHERE serverId = :id")
    suspend fun getByServerId(id: Int): AccountEntity?

    @Query("DELETE FROM account WHERE localId = :id")
    suspend fun delete(id: String)

    @Update
    suspend fun update(account: AccountEntity)

    @Query("SELECT * FROM account WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingSync(): List<AccountEntity>

    @Query("UPDATE account SET serverId = :serverId, syncStatus = :syncStatus, updatedAt = :updatedAt WHERE localId = :localId")
    suspend fun updateServerId(
        localId: String,
        serverId: Int,
        syncStatus: SyncStatus,
        updatedAt: String
    )
}