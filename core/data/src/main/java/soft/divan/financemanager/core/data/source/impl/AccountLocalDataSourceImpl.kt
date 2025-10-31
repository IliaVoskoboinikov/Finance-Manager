package soft.divan.financemanager.core.data.source.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.finansemanager.core.database.dao.AccountDao
import soft.divan.finansemanager.core.database.entity.AccountEntity
import javax.inject.Inject

class AccountLocalDataSourceImpl @Inject constructor(
    private val accountDao: AccountDao,
) : AccountLocalDataSource {
    override suspend fun getAccounts(): Flow<List<AccountEntity>> {
        return accountDao.getAccounts()
    }

    override suspend fun insertAccounts(accounts: List<AccountEntity>) {
        return accountDao.insertAccounts(accounts)
    }
}