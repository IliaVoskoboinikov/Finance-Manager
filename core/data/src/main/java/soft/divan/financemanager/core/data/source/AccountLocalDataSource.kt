package soft.divan.financemanager.core.data.source

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.database.entity.AccountEntity

interface AccountLocalDataSource {
    suspend fun create(account: AccountEntity)
    fun getAll(): Flow<List<AccountEntity>>
    suspend fun getByLocalId(id: String): AccountEntity?
    suspend fun getByServerId(id: String): AccountEntity?

    /** Локальные счета, соответствующие серверным [ids] — включая ещё не подтверждённые. */
    suspend fun getBySyncIds(ids: List<String>): List<AccountEntity>

    suspend fun getPendingSync(): List<AccountEntity>
    suspend fun update(account: AccountEntity)
    suspend fun delete(id: String)
    suspend fun deleteAll()
}
