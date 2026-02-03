package soft.divan.finansemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import soft.divan.finansemanager.core.database.entity.AccountEntity

@Dao
interface AccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: AccountEntity)

    @Query("SELECT * FROM account")
    fun getAll(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM account WHERE localId = :id")
    suspend fun getByLocalId(id: String): AccountEntity?

    @Query("SELECT * FROM account WHERE serverId = :id")
    suspend fun getByServerId(id: Int): AccountEntity?

    @Query("SELECT * FROM account WHERE serverId IN (:serverIds)")
    suspend fun getByServerIds(serverIds: List<Int>): List<AccountEntity>

    @Query("SELECT * FROM account WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingSync(): List<AccountEntity>

    @Update
    suspend fun update(account: AccountEntity)

    @Query("DELETE FROM account WHERE localId = :id")
    suspend fun delete(id: String)
}
