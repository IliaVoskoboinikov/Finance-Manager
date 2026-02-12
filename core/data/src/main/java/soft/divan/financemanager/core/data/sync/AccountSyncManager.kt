package soft.divan.financemanager.core.data.sync

import soft.divan.financemanager.core.data.dto.CreateAccountRequestDto
import soft.divan.financemanager.core.data.sync.util.Syncable
import soft.divan.finansemanager.core.database.entity.AccountEntity

interface AccountSyncManager : Syncable {
    suspend fun pullServerData()
    suspend fun syncCreate(accountDto: CreateAccountRequestDto, localId: String)
    suspend fun syncUpdate(accountEntity: AccountEntity)
    suspend fun syncDelete(accountEntity: AccountEntity)
}
