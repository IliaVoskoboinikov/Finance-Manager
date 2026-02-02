package soft.divan.financemanager.core.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
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
import soft.divan.financemanager.core.data.util.safeApiCall
import soft.divan.financemanager.core.data.util.safeDbCall
import soft.divan.financemanager.core.data.util.safeDbFlow
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.domain.result.fold
import soft.divan.financemanager.core.domain.result.onSuccess
import soft.divan.financemanager.core.loggingerror.api.ErrorLogger
import soft.divan.finansemanager.core.database.entity.AccountEntity
import soft.divan.finansemanager.core.database.model.SyncStatus
import javax.inject.Inject

class AccountRepositoryImpl @Inject constructor(
    private val accountRemoteDataSource: AccountRemoteDataSource,
    private val accountLocalDataSource: AccountLocalDataSource,
    private val transactionLocalDataSource: TransactionLocalDataSource,
    private val accountSyncManager: AccountSyncManager,
    private val applicationScope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    private val exceptionHandler: CoroutineExceptionHandler,
    private val errorLogger: ErrorLogger
) : AccountRepository {

    /** Сразу получаем поток данных с БД и сразу запускаем синхронизацию */
    override fun getAccounts(): Flow<DomainResult<List<Account>>> {
        applicationScope.launch(dispatcher + exceptionHandler) {
            accountSyncManager.pullServerData()
        }
        return safeDbFlow(errorLogger) {
            accountLocalDataSource.getAccounts().map { list ->
                list.filter { it.syncStatus != SyncStatus.PENDING_DELETE }.map { it.toDomain() }
            }
        }
    }

    /** Создаем аккаунт в БД и сразу запускаем синхронизацию */
    override suspend fun createAccount(account: Account): DomainResult<Unit> {
        applicationScope.launch(dispatcher + exceptionHandler) {
            accountSyncManager.syncCreate(accountDto = account.toDto(), localId = account.id)
        }
        return safeDbCall(errorLogger) {
            accountLocalDataSource.createAccount(
                account.toEntity(serverId = null, syncStatus = SyncStatus.PENDING_CREATE)
            )
        }
    }

    /**  Хелпер для получения локального аккаунта*/
    private suspend fun getLocalAccountOrFail(id: String): DomainResult<AccountEntity> {
        return safeDbCall(errorLogger) {
            accountLocalDataSource.getByLocalId(id)
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

    /** Вытаскиваем из бд аккаунт(так как только в БД храним serverId) обновляем локальный аккаунт и
     * запускаем синхронизацию если аккаунт не синхронизирован с сервером то создаем на сервере и
     * обновляем локально, иначе просто обновляем на сервере */
    override suspend fun updateAccount(account: Account): DomainResult<Unit> {
        val resultDb = getLocalAccountOrFail(account.id)
        if (resultDb is DomainResult.Failure) return resultDb

        val accountEntity = (resultDb as DomainResult.Success).data

        applicationScope.launch(dispatcher + exceptionHandler) {
            if (accountEntity.serverId == null) {
                /** если аккаунт не синхронизирован с сервером (нету serverId) то создать на сервере и
                 *  обновить локально*/
                accountSyncManager.syncCreate(accountDto = account.toDto(), localId = account.id)
            } else {
                accountSyncManager.syncUpdate(
                    account.toEntity(
                        serverId = accountEntity.serverId,
                        syncStatus = SyncStatus.PENDING_UPDATE
                    )
                )
            }
        }

        return safeDbCall(errorLogger) {
            accountLocalDataSource.updateAccount(
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
     * если на сервере аккаунта нету то просто удалем, если есть то удаляем на сервере и локально */
    @Suppress("ReturnCount")
    override suspend fun deleteAccount(id: String): DomainResult<Unit> {
        val localResult = getLocalAccountOrFail(id)

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

        applicationScope.launch(dispatcher + exceptionHandler) {
            accountSyncManager.syncDelete(accountEntity)
        }

        return safeDbCall(errorLogger) {
            accountLocalDataSource.updateAccount(
                accountEntity.copy(syncStatus = SyncStatus.PENDING_DELETE)
            )
        }
    }

    /**
     * 1. Получаем аккаунт из локальной БД (источник истины)
     * 2. Возвращаем его сразу (offline-first)
     * 3. В фоне:
     *    - если есть serverId → обновляем с сервера
     *    - если нет → пытаемся создать на сервере
     */
    override suspend fun getAccountById(id: String): DomainResult<Account> {
        val localResult = getLocalAccountOrFail(id)
        if (localResult is DomainResult.Failure) return localResult

        val accountEntity = (localResult as DomainResult.Success).data

        applicationScope.launch(dispatcher + exceptionHandler) {
            val serverId = accountEntity.serverId
            if (serverId != null) {
                safeApiCall(errorLogger) { accountRemoteDataSource.getById(serverId) }.onSuccess { accountDto ->
                    safeDbCall(errorLogger) {
                        accountLocalDataSource.updateAccount(
                            accountDto.toEntity(
                                localId = accountEntity.localId,
                                syncStatus = SyncStatus.SYNCED
                            )
                        )
                    }
                }
            } else {
                /** аккаунт не синхронизирован с сервером то создаем на сервере*/
                accountSyncManager.syncCreate(accountDto = accountEntity.toDto(), localId = id)
            }
        }

        return DomainResult.Success(accountEntity.toDomain())
    }
}
