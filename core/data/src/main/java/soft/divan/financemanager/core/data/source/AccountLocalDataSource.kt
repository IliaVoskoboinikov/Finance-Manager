package soft.divan.financemanager.core.data.source

import kotlinx.coroutines.flow.Flow
import soft.divan.finansemanager.core.database.entity.AccountEntity

interface AccountLocalDataSource {
    suspend fun getAccounts(): Flow<List<AccountEntity>>
    suspend fun insertAccounts(accounts: List<AccountEntity>)
    suspend fun deleteAccount(id: String)
    suspend fun getAccountByLocalId(id: String): AccountEntity?
    suspend fun getAccountByServerId(id: Int): AccountEntity?
    suspend fun updateAccount(account: AccountEntity)
    suspend fun createAccount(account: AccountEntity)
    suspend fun getPendingSync(): List<AccountEntity>
}