package soft.divan.financemanager.core.data.sync.impl

import soft.divan.financemanager.core.data.dto.AccountDto
import soft.divan.financemanager.core.data.dto.CreateAccountRequestDto
import soft.divan.financemanager.core.data.mapper.toDto
import soft.divan.financemanager.core.data.mapper.toEntity
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.financemanager.core.data.source.AccountRemoteDataSource
import soft.divan.financemanager.core.data.sync.AccountSyncManager
import soft.divan.financemanager.core.data.sync.util.Syncable
import soft.divan.financemanager.core.data.sync.util.Synchronizer
import soft.divan.financemanager.core.data.util.generateUUID
import soft.divan.financemanager.core.data.util.safeApiCall
import soft.divan.financemanager.core.data.util.safeDbCall
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.domain.result.onSuccess
import soft.divan.financemanager.core.loggingerror.api.ErrorLogger
import soft.divan.finansemanager.core.database.entity.AccountEntity
import soft.divan.finansemanager.core.database.model.SyncStatus
import javax.inject.Inject

class AccountSyncManagerImpl @Inject constructor(
    private val accountRemoteDataSource: AccountRemoteDataSource,
    private val accountLocalDataSource: AccountLocalDataSource,
    private val errorLogger: ErrorLogger
) : AccountSyncManager, Syncable {

    /** Запуск синхронизации через workManager */
    override suspend fun syncWith(synchronizer: Synchronizer): Boolean {
        return runCatching {
            pullServerData()
            pushLocalChanges()
        }.isSuccess
    }

    /** Получаем данные с сервера и обновляем локальную БД разрешая конфликты */
    override suspend fun pullServerData() {
        safeApiCall(errorLogger) { accountRemoteDataSource.getAccounts() }.onSuccess { accountDtos ->
            accountDtos.forEach { accountDto ->
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

    /** Создаем на серевере и обновляем локальную БД */
    override suspend fun syncCreate(accountDto: CreateAccountRequestDto, localId: String) {
        safeApiCall(errorLogger) {
            accountRemoteDataSource.createAccount(accountDto)
        }.onSuccess { accountDto ->
            updateLocalFromRemote(accountDto = accountDto, localId = localId)
        }
    }

    /** Обновляем на сервере и обновляем локальную БД */
    override suspend fun syncUpdate(accountEntity: AccountEntity) {
        accountEntity.serverId?.let { idAccount ->
            safeApiCall(errorLogger) {
                accountRemoteDataSource.updateAccount(
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
        // todo улучшить нейминг удаления по локал айди
        if (accountEntity.serverId == null) {
            safeDbCall(errorLogger) { accountLocalDataSource.deleteAccount(accountEntity.localId) }
        } else {
            safeApiCall(errorLogger) { accountRemoteDataSource.delete(accountEntity.serverId!!) }.onSuccess {
                safeDbCall(errorLogger) { accountLocalDataSource.deleteAccount(accountEntity.localId) }
            }
        }
    }

    /** Получаем все локальные данныве которые ожидают синхронизацю и запускаем синхронизацию */
    private suspend fun pushLocalChanges() {
        safeDbCall(errorLogger) { accountLocalDataSource.getPendingSync() }.onSuccess { accountEntities ->
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
            accountLocalDataSource.updateAccount(
                accountDto.toEntity(
                    localId = localId,
                    syncStatus = SyncStatus.SYNCED
                )
            )
        }
    }
}