package soft.divan.financemanager.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import soft.divan.financemanager.core.data.error.DataError
import soft.divan.financemanager.core.data.mapper.TimeMapper
import soft.divan.financemanager.core.data.mapper.toDomain
import soft.divan.financemanager.core.data.mapper.toDomainError
import soft.divan.financemanager.core.data.mapper.toDto
import soft.divan.financemanager.core.data.mapper.toEntity
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.financemanager.core.data.source.AccountRemoteDataSource
import soft.divan.financemanager.core.data.source.TransactionLocalDataSource
import soft.divan.financemanager.core.data.sync.AccountSyncManager
import soft.divan.financemanager.core.data.util.coroutne.AppCoroutineContext
import soft.divan.financemanager.core.data.util.safeCall.safeApiCall
import soft.divan.financemanager.core.data.util.safeCall.safeDbCall
import soft.divan.financemanager.core.data.util.safeCall.safeDbFlow
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.domain.result.fold
import soft.divan.financemanager.core.domain.result.onSuccess
import soft.divan.financemanager.core.loggingerror.api.ErrorLogger
import soft.divan.financemanager.core.database.entity.AccountEntity
import soft.divan.financemanager.core.database.model.SyncStatus
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val remoteDataSource: AccountRemoteDataSource,
    private val localDataSource: AccountLocalDataSource,
    private val transactionLocalDataSource: TransactionLocalDataSource,
    private val syncManager: AccountSyncManager,
    private val appCoroutineContext: AppCoroutineContext,
    private val errorLogger: ErrorLogger
) : AccountRepository {

    /** Создаем аккаунт в БД и сразу запускаем синхронизацию */
    override suspend fun create(account: Account): DomainResult<Unit> {
        appCoroutineContext.launch {
            syncManager.syncCreate(accountDto = account.toDto(), localId = account.id)
        }
        return safeDbCall(errorLogger) {
            localDataSource.create(
                account.toEntity(serverId = null, syncStatus = SyncStatus.PENDING_CREATE)
            )
        }
    }

    /** Сразу получаем поток данных с БД и сразу запускаем синхронизацию */
    override fun getAll(): Flow<DomainResult<List<Account>>> {
        appCoroutineContext.launch {
            syncManager.pullServerData()
        }
        return safeDbFlow(errorLogger) {
            localDataSource.getAll().map { list ->
                list.filter { it.syncStatus != SyncStatus.PENDING_DELETE }.map { it.toDomain() }
            }
        }
    }

    /**
     * 1. Получаем аккаунт из локальной БД (источник истины)
     * 2. Возвращаем его сразу (offline-first)
     * 3. В фоне:
     *    - если есть serverId → обновляем с сервера
     *    - если нет → пытаемся создать на сервере
     */
    override suspend fun getById(id: String): DomainResult<Account> {
        val localResult = getLocalOrFail(id)
        if (localResult is DomainResult.Failure) return localResult

        val accountEntity = (localResult as DomainResult.Success).data

        appCoroutineContext.launch {
            val serverId = accountEntity.serverId
            if (serverId != null) {
                safeApiCall(errorLogger) {
                    remoteDataSource.getById(serverId)
                }.onSuccess { accountDto ->
                    safeDbCall(errorLogger) {
                        localDataSource.update(
                            accountDto.toEntity(
                                localId = accountEntity.localId,
                                syncStatus = SyncStatus.SYNCED
                            )
                        )
                    }
                }
            } else {
                // Аккаунт не синхронизирован с сервером то создаем на сервере
                syncManager.syncCreate(accountDto = accountEntity.toDto(), localId = id)
            }
        }

        return DomainResult.Success(accountEntity.toDomain())
    }

    /** Вытаскиваем из бд аккаунт(так как только в БД храним serverId) обновляем локальный аккаунт и
     * запускаем синхронизацию если аккаунт не синхронизирован с сервером то создаем на сервере и
     * обновляем локально, иначе просто обновляем на сервере */
    override suspend fun update(account: Account): DomainResult<Unit> {
        val resultDb = getLocalOrFail(account.id)
        if (resultDb is DomainResult.Failure) return resultDb

        val accountEntity = (resultDb as DomainResult.Success).data

        appCoroutineContext.launch {
            if (accountEntity.serverId == null) {
                // Если аккаунт не синхронизирован с сервером (нет serverId), то создать на сервере и
                syncManager.syncCreate(accountDto = account.toDto(), localId = account.id)
            } else {
                syncManager.syncUpdate(
                    account.toEntity(
                        serverId = accountEntity.serverId,
                        syncStatus = SyncStatus.PENDING_UPDATE
                    )
                )
            }
        }

        return safeDbCall(errorLogger) {
            localDataSource.update(
                accountEntity.copy(
                    name = account.name,
                    balance = account.balance.toPlainString(),
                    currency = account.currency,
                    createdAt = TimeMapper.toApi(account.createdAt),
                    updatedAt = TimeMapper.toApi(account.updatedAt),
                    syncStatus = if (accountEntity.serverId == null) {
                        SyncStatus.PENDING_CREATE
                    } else {
                        SyncStatus.PENDING_UPDATE
                    }
                )
            )
        }
    }

    /** Получаем аккаунт из БД и и проверяем есть ли у него транзакции если нет то помечаем в БД
     * как удаленный и запускаем синхронизацию удаления,
     * если на сервере аккаунта нет то просто удалем, если есть то удаляем на сервере и локально */
    @Suppress("ReturnCount")
    override suspend fun delete(id: String): DomainResult<Unit> {
        val localResult = getLocalOrFail(id)

        if (localResult is DomainResult.Failure) return localResult

        val accountEntity = (localResult as DomainResult.Success).data

        val hasTransactions = safeDbCall(errorLogger) {
            transactionLocalDataSource.getByAccountId(id).isNotEmpty()
        }
        // todo доработать, по серверной логике нельзя удалять счет если на нем есть операции
        if (hasTransactions is DomainResult.Success && hasTransactions.data) {
            return DomainResult.Failure(
                DataError.LocalDb(Throwable("Account has transactions")).toDomainError()
            )
        }

        appCoroutineContext.launch {
            syncManager.syncDelete(accountEntity)
        }

        return safeDbCall(errorLogger) {
            localDataSource.update(
                accountEntity.copy(syncStatus = SyncStatus.PENDING_DELETE)
            )
        }
    }

    /**  Хелпер для получения локального аккаунта*/
    private suspend fun getLocalOrFail(id: String): DomainResult<AccountEntity> {
        return safeDbCall(errorLogger) {
            localDataSource.getByLocalId(id)
        }.fold(
            onSuccess = { entity ->
                entity?.let { DomainResult.Success(it) }
                    ?: DomainResult.Failure(DataError.NotFound.toDomainError())
            },
            onFailure = { error ->
                DomainResult.Failure(error)
            }
        )
    }
}
