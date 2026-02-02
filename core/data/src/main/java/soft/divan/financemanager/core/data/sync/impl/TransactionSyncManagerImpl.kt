package soft.divan.financemanager.core.data.sync.impl

import kotlinx.coroutines.flow.first
import soft.divan.financemanager.core.data.mapper.ApiDateMapper
import soft.divan.financemanager.core.data.mapper.toDto
import soft.divan.financemanager.core.data.mapper.toEntity
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.financemanager.core.data.source.TransactionLocalDataSource
import soft.divan.financemanager.core.data.source.TransactionRemoteDataSource
import soft.divan.financemanager.core.data.sync.TransactionSyncManager
import soft.divan.financemanager.core.data.sync.util.Syncable
import soft.divan.financemanager.core.data.sync.util.Synchronizer
import soft.divan.financemanager.core.data.util.generateUUID
import soft.divan.financemanager.core.data.util.safeApiCall
import soft.divan.financemanager.core.data.util.safeDbCall
import soft.divan.financemanager.core.domain.result.getOrNull
import soft.divan.financemanager.core.domain.result.onSuccess
import soft.divan.financemanager.core.loggingerror.api.ErrorLogger
import soft.divan.finansemanager.core.database.entity.TransactionEntity
import soft.divan.finansemanager.core.database.model.SyncStatus
import java.time.Instant
import javax.inject.Inject

class TransactionSyncManagerImpl @Inject constructor(
    private val remoteDataSource: TransactionRemoteDataSource,
    private val localDataSource: TransactionLocalDataSource,
    private val accountLocalDataSource: AccountLocalDataSource,
    private val errorLogger: ErrorLogger
) : TransactionSyncManager, Syncable {

    /** Запуск синхронизации через workManager */
    override suspend fun syncWith(synchronizer: Synchronizer): Boolean {
        return runCatching {
            pullServerData()
            pushLocalChanges()
        }.isSuccess
    }

    /** Получаем все локальные акаунты и тянем с сервера все транзакции с момента создания аккаунта
     * и до сегодняшнего дня */
    override suspend fun pullServerData() {
        accountLocalDataSource.getAccounts().first().forEach { account ->
            pullTransactionsFromRemoteForAccount(
                accountLocalId = account.localId,
                startDate = ApiDateMapper.toApiDate(Instant.parse(account.createdAt)),
                endDate = ApiDateMapper.toApiDate(Instant.now())
            )
        }
    }

    /** Создаем на серевер транзакцию и обновляем локальную БД */
    override suspend fun syncCreate(transactionEntity: TransactionEntity) {
        transactionEntity.accountServerId?.let { accountServerId ->
            safeApiCall(errorLogger) {
                remoteDataSource.createTransaction(transactionEntity.toDto(accountServerId))
            }.onSuccess { transactionRequestDto ->
                updateLocalFromRemote(
                    transactionRequestDto.toEntity(
                        localId = transactionEntity.localId,
                        accountLocalId = transactionEntity.accountLocalId,
                        currencyCode = transactionEntity.currencyCode,
                        syncStatus = SyncStatus.SYNCED
                    )
                )
            }
        }
    }

    /** Обновляем на сервере и обновляем локальную БД */
    override suspend fun syncUpdate(transactionEntity: TransactionEntity) {
        transactionEntity.serverId?.let { serverId ->
            transactionEntity.accountServerId?.let { accountServerId ->
                safeApiCall(errorLogger) {
                    remoteDataSource.updateTransaction(
                        id = serverId,
                        transaction = transactionEntity.toDto(accountServerId)
                    )
                }.onSuccess { transactionDto ->
                    updateLocalFromRemote(
                        transactionDto.toEntity(
                            localId = transactionEntity.localId,
                            accountLocalId = transactionEntity.accountLocalId,
                            syncStatus = SyncStatus.SYNCED
                        )
                    )
                }
            }
        }
    }

    /** Удаляем на сервере и удалем локально из БД */
    override suspend fun syncDelete(transactionEntity: TransactionEntity) {
        if (transactionEntity.serverId == null) {
            deleteLocalTransaction(transactionEntity.localId)
        } else {
            safeApiCall(errorLogger) {
                remoteDataSource.delete(transactionEntity.serverId!!)
            }.onSuccess {
                deleteLocalTransaction(transactionEntity.localId)
            }
        }
    }

    /** Получаем все локальные данныве которые ожидают синхронизацю и запускаем синхронизацию */
    private suspend fun pushLocalChanges() {
        safeDbCall(errorLogger) {
            localDataSource.getPendingSync()
        }.onSuccess { transactionEntities ->
            transactionEntities.forEach { transactionEntity ->
                when (transactionEntity.syncStatus) {
                    SyncStatus.PENDING_CREATE -> syncCreate(transactionEntity)
                    SyncStatus.PENDING_UPDATE -> syncUpdate(transactionEntity)
                    SyncStatus.PENDING_DELETE -> syncDelete(transactionEntity)
                    SyncStatus.SYNCED -> Unit
                }
            }
        }
    }

    override suspend fun pullTransactionsFromRemoteForAccount(
        accountLocalId: String,
        startDate: String,
        endDate: String
    ) {
        // если аккаунт ещё не синхронизирован — транзакции с сервера не тянем
        val serverAccountId = getServerAccountIdByLocalId(accountLocalId) ?: return

        safeApiCall(errorLogger) {
            remoteDataSource.getTransactionsByAccountAndPeriod(
                serverAccountId,
                startDate,
                endDate
            )
        }.onSuccess { transactionDtos ->
            val serverIds = transactionDtos.map { it.id }

            val localTransactions = safeDbCall(errorLogger) {
                localDataSource.getTransactionsByServerIds(serverIds)
            }.getOrNull().orEmpty()

            val localMap = localTransactions.associateBy { it.serverId }

            transactionDtos.forEach { transactionDto ->
                val localTransaction = localMap[transactionDto.id]

                if (localTransaction == null) {
                    /**  Локальной транзакции нет → создаём */
                    safeDbCall(errorLogger) {
                        localDataSource.createTransaction(
                            transactionDto.toEntity(
                                localId = generateUUID(),
                                accountLocalId = accountLocalId,
                                syncStatus = SyncStatus.SYNCED
                            )
                        )
                    }
                } else if (transactionDto.updatedAt > localTransaction.updatedAt) {
                    /** Если есть то разрешаем конфликт, побежадает, так которая менялась позже  */
                    updateLocalFromRemote(
                        transactionDto.toEntity(
                            localId = localTransaction.localId,
                            accountLocalId = localTransaction.accountLocalId,
                            syncStatus = SyncStatus.SYNCED
                        )
                    )
                }
            }
        }
    }

    /** Унифицируем обновление локальной транзакции */
    private suspend fun updateLocalFromRemote(transactionEntity: TransactionEntity) {
        safeDbCall(errorLogger) {
            localDataSource.updateTransaction(transactionEntity)
        }
    }

    private suspend fun deleteLocalTransaction(localId: String) {
        safeDbCall(errorLogger) {
            localDataSource.deleteTransaction(localId)
        }
    }

    private suspend fun getServerAccountIdByLocalId(id: String): Int? {
        return safeDbCall(errorLogger) {
            accountLocalDataSource.getByLocalId(id)
        }.getOrNull()?.serverId
    }
}