package soft.divan.financemanager.core.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.data.Syncable
import soft.divan.financemanager.core.data.Synchronizer
import soft.divan.financemanager.core.data.dto.AccountDto
import soft.divan.financemanager.core.data.dto.CreateAccountRequestDto
import soft.divan.financemanager.core.data.error.DataError
import soft.divan.financemanager.core.data.mapper.toDomain
import soft.divan.financemanager.core.data.mapper.toDomainError
import soft.divan.financemanager.core.data.mapper.toDto
import soft.divan.financemanager.core.data.mapper.toEntity
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.financemanager.core.data.source.AccountRemoteDataSource
import soft.divan.financemanager.core.data.source.TransactionLocalDataSource
import soft.divan.financemanager.core.data.util.generateUUID
import soft.divan.financemanager.core.data.util.safeApiCall
import soft.divan.financemanager.core.data.util.safeDbCall
import soft.divan.financemanager.core.data.util.safeDbFlow
import soft.divan.financemanager.core.domain.data.DateHelper
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.logging_error.logging_error_api.ErrorLogger
import soft.divan.finansemanager.core.database.entity.AccountEntity
import soft.divan.finansemanager.core.database.model.SyncStatus
import javax.inject.Inject

//todo почему при создании сугщyсности сервер создает дату и время обновления и создания?
// Если на одном устройстве сначала была создлана транзакция
// а устройство было без сети и на другом утсройстве после этого была создана у которого была сеть то
// потом у первого утройства появилаьсь сеть то транзакция которапя была реально раньше окажется созданной позже!!!!
// как буто дата создания и обновыления должны быть истинными на устройтсве а не на сервере

class AccountRepositoryImpl @Inject constructor(
    private val accountRemoteDataSource: AccountRemoteDataSource,
    private val accountLocalDataSource: AccountLocalDataSource,
    private val transactionLocalDataSource: TransactionLocalDataSource,
    private val applicationScope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    private val exceptionHandler: CoroutineExceptionHandler,
    private val errorLogger: ErrorLogger
) : AccountRepository, Syncable {

    /** Сразу получаем поток данных с БД и сразу запускаем синхронизацию */
    override suspend fun getAccounts(): Flow<DomainResult<List<Account>>> {
        applicationScope.launch(dispatcher + exceptionHandler) {
            pullServerData()
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
            syncCreate(accountDto = account.toDto(), localId = account.id)
        }
        return safeDbCall(errorLogger) {
            accountLocalDataSource.createAccount(
                account.toEntity(serverId = null, syncStatus = SyncStatus.PENDING_CREATE)
            )
        }
    }

    /** Вытаскиваем из бд аккаунт(так как только в БД храним serverId)  обновляем локальный аккаунт и запускаем синхронизацию
     * если аккаунт не синхронизирован с сервером то создаем на сервере и обновляем локально, иначе просто обновляем на сервере */
    override suspend fun updateAccount(account: Account): DomainResult<Unit> {
        val resultDb = safeDbCall(errorLogger) { accountLocalDataSource.getAccountByLocalId(account.id) }
        if (resultDb is DomainResult.Success && resultDb.data != null) {
            applicationScope.launch(dispatcher + exceptionHandler) {
                val serverId = resultDb.data!!.serverId
                if (serverId != null) {
                    syncUpdate(account.toEntity(serverId, SyncStatus.PENDING_UPDATE))
                } else {
                    /** если аккаунт не синхронизирован с сервером (нету serverId) то создать на сервере и обновить локально*/
                    syncCreate(accountDto = account.toDto(), localId = account.id)
                }
            }
            return safeDbCall(errorLogger) {
                accountLocalDataSource.updateAccount(
                    resultDb.data!!.copy(
                        name = account.name,
                        balance = account.balance.toPlainString(),
                        currency = account.currency,
                        createdAt = DateHelper.dataTimeForApi(account.createdAt),
                        updatedAt = DateHelper.dataTimeForApi(account.updatedAt),
                        syncStatus = if (resultDb.data!!.serverId != null) SyncStatus.PENDING_UPDATE else SyncStatus.PENDING_CREATE
                    )
                )
            }

        } else if (resultDb is DomainResult.Success && resultDb.data == null) {
            return DomainResult.Failure(DataError.NotFound.toDomainError())
        } else {
            return resultDb as DomainResult.Failure
        }

    }

    /** Получаем аккаунт из БД и и проверяем есть ли у него транзакции если нет то помечаем в БД как удаленный и запускаем синхронизацию удаления,
     * если на сервере аккаунта нету то просто удалем, если есть то удаляем на сервере и локально */
    override suspend fun deleteAccount(id: String): DomainResult<Unit> {
        val resultDb = safeDbCall(errorLogger) { accountLocalDataSource.getAccountByLocalId(id) }
        if (resultDb is DomainResult.Success && resultDb.data != null) {
            val result =
                safeDbCall(errorLogger) { transactionLocalDataSource.getByAccountId(id).isNotEmpty() }
            if (result is DomainResult.Failure || result is DomainResult.Success && result.data) {
                //todo доработать, по серверной логике нельзя удалять счет если на нем есть операции
                return DomainResult.Failure(
                    DataError.LocalDb(Throwable("Account has transactions")).toDomainError()
                )
            }

            applicationScope.launch(dispatcher + exceptionHandler) {
                val serverId = resultDb.data!!.serverId
                if (serverId == null) {
                    safeDbCall(errorLogger) { accountLocalDataSource.deleteAccount(id) }
                } else {
                    syncDelete(resultDb.data!!)
                }
            }

            return safeDbCall(errorLogger) {
                accountLocalDataSource.updateAccount(resultDb.data!!.copy(syncStatus = SyncStatus.PENDING_DELETE))
            }
        } else if (resultDb is DomainResult.Success && resultDb.data == null) {
            return DomainResult.Failure(DataError.NotFound.toDomainError())
        } else {
            return resultDb as DomainResult.Failure
        }
    }

    /** Сразу возворащаем аккаунт из БД и пытаемся синхронизировать его с сервером */
    override suspend fun getAccountById(id: String): DomainResult<Account?> {
        applicationScope.launch(dispatcher + exceptionHandler) {
            val resultDb = safeDbCall(errorLogger) { accountLocalDataSource.getAccountByLocalId(id) }
            if (resultDb is DomainResult.Success && resultDb.data != null) {
                val serverId = resultDb.data!!.serverId
                if (serverId != null) {
                    val resultApi =
                        safeApiCall(errorLogger) { accountRemoteDataSource.getById(serverId) }
                    if (resultApi is DomainResult.Success) {
                        safeDbCall(errorLogger) {
                            accountLocalDataSource.updateAccount(
                                resultApi.data.toEntity(
                                    localId = resultDb.data!!.localId,
                                    syncStatus = SyncStatus.SYNCED
                                )
                            )
                        }
                    }
                } else {
                    /** аккаунт не синхронизирован с сервером то создаем на сервере*/
                    syncCreate(accountDto = resultDb.data!!.toDto(), localId = id)
                }
            }
        }

        return safeDbCall(errorLogger) { accountLocalDataSource.getAccountByLocalId(id)?.toDomain() }
    }


////////////////// Sync //////////////////

    /** Запуск синхронизации через workManager */
    override suspend fun syncWith(synchronizer: Synchronizer): Boolean {
        return runCatching { sync() }.isSuccess
    }

    private suspend fun sync() {
        pullServerData()
        pushLocalChanges()
    }

    /** Получаем данные с сервера и обновляем локальную БД разрешая конфликты */
    private suspend fun pullServerData() {
        val resultApi = safeApiCall(errorLogger) { accountRemoteDataSource.getAccounts() }
        if (resultApi is DomainResult.Success) {
            resultApi.data.let { accountsDto ->
                accountsDto.forEach { accountDto ->
                    val resultDb =
                        safeDbCall(errorLogger) { accountLocalDataSource.getAccountByServerId(accountDto.id) }
                    if (resultDb is DomainResult.Success && resultDb.data == null) {
                        /** новаый аккаунт на сервере → создаем его локально*/
                        safeDbCall(errorLogger) {
                            accountLocalDataSource.createAccount(
                                accountDto.toEntity(
                                    localId = generateUUID(),
                                    syncStatus = SyncStatus.SYNCED
                                )
                            )
                        }
                        /** если конфликт то сравниваем по времени обновления, побеждает тот кто поослдений был обновлен */
                    } else if (resultDb is DomainResult.Success && accountDto.updatedAt > resultDb.data!!.updatedAt) {
                        updateLocalFromRemote(
                            accountDto = accountDto,
                            localId = resultDb.data!!.localId
                        )
                    }
                }
            }
        }
    }

    /** Унифицируем обновление локального аккаунта после ответа сервера */
    private suspend fun updateLocalFromRemote(accountDto: AccountDto, localId: String) {
        safeDbCall(errorLogger) {
            accountLocalDataSource.updateAccount(
                accountDto.toEntity(
                    localId = localId,
                    syncStatus = SyncStatus.SYNCED
                )
            )
        }
    }

    /** Получаем все локальные данныве которые ожидают синхронизацю и запускаем синхронизацию */
    private suspend fun pushLocalChanges() {
        val resultDb = safeDbCall(errorLogger) { accountLocalDataSource.getPendingSync() }
        if (resultDb is DomainResult.Success) {
            resultDb.data.forEach { accountEntity ->
                when (accountEntity.syncStatus) {
                    SyncStatus.SYNCED -> Unit
                    SyncStatus.PENDING_CREATE -> syncCreate(
                        accountDto = accountEntity.toDto(),
                        localId = accountEntity.localId
                    )

                    SyncStatus.PENDING_UPDATE -> syncUpdate(accountEntity)
                    SyncStatus.PENDING_DELETE -> syncDelete(accountEntity)
                }
            }
        }
    }

    /** Создаем на серевер и обновляем локальную БД */
    private suspend fun syncCreate(accountDto: CreateAccountRequestDto, localId: String) {
        val resultApi = safeApiCall(errorLogger) { accountRemoteDataSource.createAccount(accountDto) }
        if (resultApi is DomainResult.Success) {
            updateLocalFromRemote(resultApi.data, localId)
        }
    }

    /** Обновляем на сервере и обновляем локальную БД */
    private suspend fun syncUpdate(accountEntity: AccountEntity) {
        accountEntity.serverId?.let { idAccount ->
            val resultApi = safeApiCall(errorLogger) {
                accountRemoteDataSource.updateAccount(id = idAccount, account = accountEntity.toDto())
            }
            if (resultApi is DomainResult.Success) {
                updateLocalFromRemote(accountDto = resultApi.data, localId = accountEntity.localId)
            }
        }
    }

    /** Удаляем на сервере и удалем локально из БД */
    private suspend fun syncDelete(accountEntity: AccountEntity) {
        accountEntity.serverId?.let { idAccount ->
            val resultApi = safeApiCall(errorLogger) { accountRemoteDataSource.delete(idAccount) }
            if (resultApi is DomainResult.Success) {
                safeDbCall(errorLogger) { accountLocalDataSource.deleteAccount(accountEntity.localId) }
            }
        }
    }

}