package soft.divan.financemanager.core.data.sync.impl

import soft.divan.financemanager.core.data.dto.AccountDto
import soft.divan.financemanager.core.data.dto.CreateAccountRequestDto
import soft.divan.financemanager.core.data.mapper.toDto
import soft.divan.financemanager.core.data.mapper.toEntity
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.financemanager.core.data.source.AccountRemoteDataSource
import soft.divan.financemanager.core.data.sync.AccountSyncManager
import soft.divan.financemanager.core.data.sync.util.Synchronizer
import soft.divan.financemanager.core.data.util.generateUUID
import soft.divan.financemanager.core.data.util.safeCall.safeApiCall
import soft.divan.financemanager.core.data.util.safeCall.safeDbCall
import soft.divan.financemanager.core.domain.result.getOrNull
import soft.divan.financemanager.core.domain.result.onSuccess
import soft.divan.financemanager.core.loggingerror.api.ErrorLogger
import soft.divan.finansemanager.core.database.entity.AccountEntity
import soft.divan.finansemanager.core.database.model.SyncStatus
import javax.inject.Inject

class AccountSyncManagerImpl @Inject constructor(
    private val remoteDataSource: AccountRemoteDataSource,
    private val localDataSource: AccountLocalDataSource,
    private val errorLogger: ErrorLogger
) : AccountSyncManager {

    /** Запуск синхронизации через workManager */
    override suspend fun syncWith(synchronizer: Synchronizer): Boolean {
        return runCatching {
            pullServerData()
            pushLocalChanges()
        }.isSuccess
    }

    /** Получаем данные с сервера и обновляем локальную БД разрешая конфликты */
    override suspend fun pullServerData() {
        safeApiCall(errorLogger) {
            remoteDataSource.getAll()
        }.onSuccess { accountDtos ->

            val serverIds = accountDtos.map { it.id }

            val localAccounts = safeDbCall(errorLogger) {
                localDataSource.getByServerIds(serverIds)
            }.getOrNull().orEmpty()

            val localMap = localAccounts.associateBy { it.serverId }

            accountDtos.forEach { accountDto ->
                val localAccount = localMap[accountDto.id]

                if (localAccount == null) {
                    /** Локального аккаунта нет → создаём */
                    safeDbCall(errorLogger) {
                        localDataSource.create(
                            accountDto.toEntity(
                                localId = generateUUID(),
                                syncStatus = SyncStatus.SYNCED
                            )
                        )
                    }
                } else if (accountDto.updatedAt > localAccount.updatedAt) {
                    /** Конфликт → побеждает тот, кто обновлялся позже */
                    updateLocalFromRemote(
                        accountDto = accountDto,
                        localId = localAccount.localId
                    )
                }
            }
        }
    }

    /** Создаем на серевере и обновляем локальную БД */
    override suspend fun syncCreate(accountDto: CreateAccountRequestDto, localId: String) {
        safeApiCall(errorLogger) {
            remoteDataSource.create(accountDto)
        }.onSuccess { accountDto ->
            updateLocalFromRemote(accountDto = accountDto, localId = localId)
        }
    }

    /** Обновляем на сервере и обновляем локальную БД */
    override suspend fun syncUpdate(accountEntity: AccountEntity) {
        accountEntity.serverId?.let { idAccount ->
            safeApiCall(errorLogger) {
                remoteDataSource.update(
                    id = idAccount,
                    account = accountEntity.toDto()
                )
            }.onSuccess { accountDto ->
                updateLocalFromRemote(accountDto = accountDto, localId = accountEntity.localId)
            }
        }
    }

    /** Удаляем на сервере и удалем локально из БД */
    override suspend fun syncDelete(accountEntity: AccountEntity) {
        if (accountEntity.serverId == null) {
            deleteLocalAccount(accountEntity.localId)
        } else {
            safeApiCall(errorLogger) {
                remoteDataSource.delete(accountEntity.serverId!!)
            }.onSuccess {
                deleteLocalAccount(accountEntity.localId)
            }
        }
    }

    /** Получаем все локальные данныве которые ожидают синхронизацю и запускаем синхронизацию */
    private suspend fun pushLocalChanges() {
        safeDbCall(errorLogger) { localDataSource.getPendingSync() }.onSuccess { accountEntities ->
            accountEntities.forEach { accountEntity ->
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

    /** Унифицируем обновление локального аккаунта после ответа сервера */
    private suspend fun updateLocalFromRemote(accountDto: AccountDto, localId: String) {
        safeDbCall(errorLogger) {
            localDataSource.update(
                accountDto.toEntity(
                    localId = localId,
                    syncStatus = SyncStatus.SYNCED
                )
            )
        }
    }

    private suspend fun deleteLocalAccount(localId: String) {
        safeDbCall(errorLogger) {
            localDataSource.delete(localId)
        }
    }
}
