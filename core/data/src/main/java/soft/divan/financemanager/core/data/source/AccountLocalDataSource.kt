package soft.divan.financemanager.core.data.source

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.database.entity.AccountEntity

interface AccountLocalDataSource {
    suspend fun create(account: AccountEntity)
    suspend fun getAll(): Flow<List<AccountEntity>>
    suspend fun getByLocalId(id: String): AccountEntity?
    suspend fun getByServerId(id: Int): AccountEntity?
    suspend fun getByServerIds(serverIds: List<Int>): List<AccountEntity>
    suspend fun getPendingSync(): List<AccountEntity>
    suspend fun update(account: AccountEntity)
    suspend fun delete(id: String)
}
