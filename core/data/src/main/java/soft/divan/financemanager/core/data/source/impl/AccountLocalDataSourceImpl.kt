package soft.divan.financemanager.core.data.source.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.finansemanager.core.database.dao.AccountDao
import soft.divan.finansemanager.core.database.entity.AccountEntity
import javax.inject.Inject

class AccountLocalDataSourceImpl @Inject constructor(
    private val accountDao: AccountDao,
) : AccountLocalDataSource {

    override suspend fun create(account: AccountEntity) = accountDao.insert(account)

    override suspend fun getAll(): Flow<List<AccountEntity>> = accountDao.getAll()

    override suspend fun getByLocalId(id: String): AccountEntity? = accountDao.getByLocalId(id)

    override suspend fun getByServerId(id: Int): AccountEntity? = accountDao.getByServerId(id)

    override suspend fun getByServerIds(serverIds: List<Int>): List<AccountEntity> =
        if (serverIds.isEmpty()) emptyList() else accountDao.getByServerIds(serverIds)

    override suspend fun getPendingSync(): List<AccountEntity> = accountDao.getPendingSync()

    override suspend fun update(account: AccountEntity) = accountDao.update(account)

    override suspend fun delete(id: String) = accountDao.delete(id)
}
