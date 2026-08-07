package soft.divan.financemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.database.entity.AccountEntity

@Dao
interface AccountDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: AccountEntity)

    @Query("SELECT * FROM account")
    fun getAll(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM account WHERE localId = :id")
    suspend fun getByLocalId(id: String): AccountEntity?

    @Query("SELECT * FROM account WHERE serverId = :id")
    suspend fun getByServerId(id: String): AccountEntity?

    /**
     * Локальные счета, соответствующие серверным [ids]. Используется pull'ом, чтобы понять,
     * известен ли пришедший с сервера счёт локально.
     *
     * Совпадение ищется по двум признакам:
     * 1. `serverId IN (:ids)` — обычный случай, запись уже синхронизирована;
     * 2. `localId IN (:ids) AND serverId IS NULL` — счёт создан на этом устройстве (create уходит
     *    с `id = localId`), но ACK ещё не дошёл, поэтому `serverId` не проставлен.
     *
     * Без второго условия pull не узнал бы собственный неподтверждённый счёт и вставил бы его
     * **дубликатом** с новым `localId`. Оговорка `serverId IS NULL` не даёт ложно сматчить уже
     * синхронизированную запись, у которой `localId` случайно совпал бы с чужим серверным id.
     */
    @Query(
        "SELECT * FROM account WHERE serverId IN (:ids) OR (localId IN (:ids) AND serverId IS NULL)"
    )
    suspend fun getBySyncIds(ids: List<String>): List<AccountEntity>

    @Query("SELECT * FROM account WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingSync(): List<AccountEntity>

    @Update
    suspend fun update(account: AccountEntity)

    @Query("DELETE FROM account WHERE localId = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM account")
    suspend fun deleteAll()
}
