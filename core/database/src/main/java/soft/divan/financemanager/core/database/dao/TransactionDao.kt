package soft.divan.financemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.database.entity.TransactionEntity

@Dao
interface TransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: TransactionEntity)

    @Query(
        """
    SELECT * FROM transactions
    WHERE accountLocalId = :accountId
      AND date(transactionDate, 'localtime') BETWEEN date(:startDate) AND date(:endDate)
    ORDER BY transactionDate ASC
"""
    )
    fun getByAccountAndPeriod(
        accountId: String,
        startDate: String, // "2025-10-24"
        endDate: String
    ): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE localId = :localId")
    suspend fun getByLocalId(localId: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE serverId = :serverId")
    suspend fun getByServerId(serverId: String): TransactionEntity?

    /**
     * Локальные транзакции, соответствующие серверным [ids]. Используется pull'ом, чтобы понять,
     * известна ли пришедшая с сервера запись локально.
     *
     * Совпадение ищется по двум признакам:
     * 1. `serverId IN (:ids)` — обычный случай, запись уже синхронизирована;
     * 2. `localId IN (:ids) AND serverId IS NULL` — запись создана на этом устройстве (create
     *    уходит с `id = localId`), но ACK ещё не дошёл, поэтому `serverId` не проставлен.
     *
     * Без второго условия pull не узнал бы собственную неподтверждённую транзакцию и вставил бы
     * её **дубликатом** с новым `localId`. Оговорка `serverId IS NULL` не даёт ложно сматчить
     * уже синхронизированную запись, у которой `localId` случайно совпал бы с чужим серверным id.
     */
    @Query(
        "SELECT * FROM transactions " +
            "WHERE serverId IN (:ids) OR (localId IN (:ids) AND serverId IS NULL)"
    )
    suspend fun getBySyncIds(ids: List<String>): List<TransactionEntity>

    @Query(
        "SELECT * FROM transactions WHERE accountLocalId = :accountId ORDER BY transactionDate DESC"
    )
    suspend fun getByAccountId(accountId: String): List<TransactionEntity>

    @Query("SELECT * FROM transactions WHERE syncStatus != 'SYNCED'")
    suspend fun getPendingSync(): List<TransactionEntity>

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE localId = :localId")
    suspend fun delete(localId: String)

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}
