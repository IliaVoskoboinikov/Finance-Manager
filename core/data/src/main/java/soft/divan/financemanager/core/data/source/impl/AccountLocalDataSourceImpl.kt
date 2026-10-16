package soft.divan.financemanager.core.data.source.impl

import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.financemanager.core.database.entity.AccountEntity
import soft.divan.financemanager.core.database.holder.DatabaseHolder
import javax.inject.Inject

class AccountLocalDataSourceImpl @Inject constructor(
    private val holder: DatabaseHolder
) : AccountLocalDataSource {
    override suspend fun create(account: AccountEntity) =
        holder.withDatabase { it.accountDao().insert(account) }

    override fun getAll(): Flow<List<AccountEntity>> = holder.observe { it.accountDao().getAll() }

    override suspend fun getByLocalId(id: String): AccountEntity? =
        holder.withDatabase { it.accountDao().getByLocalId(id) }

    override suspend fun getByServerId(id: String): AccountEntity? =
        holder.withDatabase { it.accountDao().getByServerId(id) }

    override suspend fun getBySyncIds(ids: List<String>): List<AccountEntity> =
        holder.withDatabase { it.accountDao().getBySyncIds(ids) }

    override suspend fun getPendingSync(): List<AccountEntity> =
        holder.withDatabase { it.accountDao().getPendingSync() }

    override suspend fun update(account: AccountEntity) =
        holder.withDatabase { it.accountDao().update(account) }

    override suspend fun delete(id: String) = holder.withDatabase { it.accountDao().delete(id) }

    override suspend fun deleteAll() = holder.withDatabase { it.accountDao().deleteAll() }
}
