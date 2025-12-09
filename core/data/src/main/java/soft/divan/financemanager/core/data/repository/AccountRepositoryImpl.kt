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
import soft.divan.financemanager.core.data.mapper.formatter
import soft.divan.financemanager.core.data.mapper.toDomain
import soft.divan.financemanager.core.data.mapper.toDto
import soft.divan.financemanager.core.data.mapper.toEntity
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.financemanager.core.data.source.AccountRemoteDataSource
import soft.divan.financemanager.core.data.source.TransactionLocalDataSource
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.finansemanager.core.database.entity.AccountEntity
import soft.divan.finansemanager.core.database.model.SyncStatus
import java.time.ZoneOffset
import java.util.UUID
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
    private val exceptionHandler: CoroutineExceptionHandler
) : AccountRepository, Syncable {

    /** Сразу получаем поток данных с БД и сразу запускаем синхронизацию */
    override suspend fun getAccounts(): Flow<List<Account>> {
        applicationScope.launch(dispatcher + exceptionHandler) {
            pullServerData()
        }
        return accountLocalDataSource.getAccounts().map { list ->
            list.filter { it.syncStatus != SyncStatus.PENDING_DELETE }.map { it.toDomain() }
        }
    }

    /** Создаем аккаунт в БД и сразу запускаем синхронизацию */
    override suspend fun createAccount(account: Account): Result<Unit> = runCatching {
        accountLocalDataSource.createAccount(
            account.toEntity(
                serverId = null,
                syncStatus = SyncStatus.PENDING_CREATE
            )
        )
        applicationScope.launch(dispatcher + exceptionHandler) {
            syncCreate(accountDto = account.toDto(), localId = account.id)
        }
    }

    /** Вытаскиваем из бд аккаунт(так как только в БД храним serverId)  обновляем локальный аккаунт и запускаем синхронизацию
     * если аккаунт не синхронизирован с сервером то создаем на сервере и обновляем локально иначе просто обновляем на сервере */
    override suspend fun updateAccount(account: Account): Result<Unit> = runCatching {
        val accountEntity = accountLocalDataSource.getAccountByLocalId(account.id)
            ?: throw IllegalStateException("Account not found")
        accountLocalDataSource.updateAccount(
            accountEntity.copy(
                name = account.name,
                balance = account.balance.toPlainString(),
                currency = account.currency,
                createdAt = account.createdAt.atOffset(ZoneOffset.UTC).format(formatter),
                updatedAt = account.updatedAt.atOffset(ZoneOffset.UTC).format(formatter),
                //todo что бы не было проблем при сихнронизации
                syncStatus = if (accountEntity.serverId != null) SyncStatus.PENDING_UPDATE else SyncStatus.PENDING_CREATE
            )
        )

        applicationScope.launch(dispatcher + exceptionHandler) {
            val serverId = accountEntity.serverId
            if (serverId != null) {
                syncUpdate(account.toEntity(serverId, SyncStatus.PENDING_UPDATE))
            } else { /// если аккаунт не синхронизирован с сервером (нету serverId) то создать на сервере и обновить локально
                syncCreate(accountDto = account.toDto(), localId = account.id)
            }
        }
    }

    /** Получаем аккаунт из БД и и проверяем есть ли у него транзакции если нет то помечаем в БД как удаленный и запускаем синхронизацию удаления,
     * если на сервере аккаунта нету то просто удалем, если есть то удаляем на сервере и локально */
    override suspend fun deleteAccount(id: String): Result<Unit> = runCatching {
        val accountEntity =
            accountLocalDataSource.getAccountByLocalId(id) ?: throw IllegalStateException(
                "Account not found"
            )
        //todo нельзя удалять счет если на нем есть операции
        //todo доделать
        if (transactionLocalDataSource.getByAccountId(id).isNotEmpty())
            throw IllegalStateException("Account has transactions")

        accountLocalDataSource.updateAccount(accountEntity.copy(syncStatus = SyncStatus.PENDING_DELETE))

        applicationScope.launch(dispatcher + exceptionHandler) {
            val serverId = accountEntity.serverId
            if (serverId == null) {
                accountLocalDataSource.deleteAccount(id)
            } else {
                syncDelete(accountEntity)
            }
        }
    }

    /** Сразу возворащаем аккаунт из БД и пытаемся синхронизировать его с сервером */
    override suspend fun getAccountById(id: String): Result<Account?> = runCatching {
        applicationScope.launch(dispatcher + exceptionHandler) {
            val accountEntity = accountLocalDataSource.getAccountByLocalId(id)
                ?: throw IllegalStateException("Account not found")

            val serverId = accountEntity.serverId
            if (serverId != null) {
                val response = accountRemoteDataSource.getById(serverId)
                if (response.isSuccessful) {
                    response.body()?.let { accountDto ->
                        accountLocalDataSource.updateAccount(
                            accountDto.toEntity(
                                localId = accountEntity.localId,
                                syncStatus = SyncStatus.SYNCED
                            )
                        )
                    }
                } else {
                    //todo сервер вернул ошибку
                }
            } else {
                //todo аккаунт не синхронизирован с сервером то создаем на сервере
                syncCreate(accountDto = accountEntity.toDto(), localId = id)
            }
        }

        return Result.success(accountLocalDataSource.getAccountByLocalId(id)?.toDomain())
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
        val response = accountRemoteDataSource.getAccounts()
        if (response.isSuccessful) {
            response.body()?.let { accountsDto ->
                accountsDto.forEach { accountDto ->
                    val accountEntity =
                        accountLocalDataSource.getAccountByServerId(accountDto.id)
                    if (accountEntity == null) {
                        /** новаый аккаунт на сервере → создаем его локально*/
                        accountLocalDataSource.createAccount(
                            accountDto.toEntity(
                                localId = UUID.randomUUID().toString(),
                                syncStatus = SyncStatus.SYNCED
                            )
                        )
                        /** если конфликт то сравниваем по времени обновления, побеждает тот кто поослдений был обновлен */
                    } else if (accountDto.updatedAt > accountEntity.updatedAt) {
                        updateLocalFromRemote(accountDto = accountDto, localId = accountEntity.localId)
                    }

                }
            }
        } else {
            //todo сервер вернул ошибку
        }
    }

    /** Унифицируем обновление локального аккаунта после ответа сервера */
    private suspend fun updateLocalFromRemote(accountDto: AccountDto, localId: String) {
        accountLocalDataSource.updateAccount(
            accountDto.toEntity(
                localId = localId,
                syncStatus = SyncStatus.SYNCED
            )
        )
    }

    /** Получаем все локальные данныве которые ожидают синхронизацю и запускаем синхронизацию */
    private suspend fun pushLocalChanges() {
        val pending = accountLocalDataSource.getPendingSync()
        for (entity in pending) {
            when (entity.syncStatus) {
                SyncStatus.SYNCED -> Unit
                SyncStatus.PENDING_CREATE -> syncCreate(
                    accountDto = entity.toDto(),
                    localId = entity.localId
                )

                SyncStatus.PENDING_UPDATE -> syncUpdate(entity)
                SyncStatus.PENDING_DELETE -> syncDelete(entity)
            }
        }
    }

    /** Создаем на серевер и обновляем локальную БД */
    private suspend fun syncCreate(accountDto: CreateAccountRequestDto, localId: String) {
        val response = accountRemoteDataSource.createAccount(accountDto)
        if (response.isSuccessful) {
            response.body()?.let { accountDto ->
                updateLocalFromRemote(accountDto = accountDto, localId = localId)
            }
        } else { //todo сервер вернул ошибку
        }
    }

    /** Обновляем на сервере и обновляем локальную БД */
    private suspend fun syncUpdate(accountEntity: AccountEntity) {
        val serverId =
            accountEntity.serverId ?: throw IllegalStateException("syncUpdate serverId is null ")
        val response =
            accountRemoteDataSource.updateAccount(id = serverId, account = accountEntity.toDto())
        if (response.isSuccessful) {
            response.body()?.let { accountDto ->
                updateLocalFromRemote(accountDto = accountDto, localId = accountEntity.localId)
            }
        } else { //todo сервер вернул ошибку
        }
    }

    /** Удаляем на сервере и удалем локально из БД */
    private suspend fun syncDelete(accountEntity: AccountEntity) {
        val serverId =
            accountEntity.serverId ?: throw IllegalStateException("syncUpdate serverId is null ")
        val response = accountRemoteDataSource.delete(serverId)
        if (response.isSuccessful) {
            accountLocalDataSource.deleteAccount(accountEntity.localId)
        } else { //todo сервер вернул ошибку
        }
    }

}