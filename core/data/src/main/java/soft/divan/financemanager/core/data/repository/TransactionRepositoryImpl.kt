package soft.divan.financemanager.core.data.repository

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import soft.divan.financemanager.core.data.Syncable
import soft.divan.financemanager.core.data.Synchronizer
import soft.divan.financemanager.core.data.error.DataError
import soft.divan.financemanager.core.data.mapper.toDomain
import soft.divan.financemanager.core.data.mapper.toDomainError
import soft.divan.financemanager.core.data.mapper.toDto
import soft.divan.financemanager.core.data.mapper.toEntity
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.financemanager.core.data.source.TransactionLocalDataSource
import soft.divan.financemanager.core.data.source.TransactionRemoteDataSource
import soft.divan.financemanager.core.data.util.generateUUID
import soft.divan.financemanager.core.data.util.safeApiCall
import soft.divan.financemanager.core.data.util.safeDbCall
import soft.divan.financemanager.core.data.util.safeDbFlow
import soft.divan.financemanager.core.domain.data.DateHelper
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.repository.TransactionRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.domain.result.fold
import soft.divan.financemanager.core.domain.result.getOrNull
import soft.divan.financemanager.core.domain.result.onSuccess
import soft.divan.financemanager.core.loggingerror.api.ErrorLogger
import soft.divan.finansemanager.core.database.entity.TransactionEntity
import soft.divan.finansemanager.core.database.model.SyncStatus
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionRemoteDataSource: TransactionRemoteDataSource,
    private val transactionLocalDataSource: TransactionLocalDataSource,
    private val accountLocalDataSource: AccountLocalDataSource,
    private val applicationScope: CoroutineScope,
    private val dispatcher: CoroutineDispatcher,
    private val exceptionHandler: CoroutineExceptionHandler,
    private val errorLogger: ErrorLogger
) : TransactionRepository, Syncable {

    /** Создаем транзакцию в БД и сразу запускаем синхронизацию */
    override suspend fun createTransaction(transaction: Transaction): DomainResult<Unit> {
        val accountServerId = accountLocalDataSource.getByLocalId(transaction.accountLocalId)?.serverId

        val transactionEntity = transaction.toEntity(
            serverId = null,
            accountServerId = accountServerId,
            syncStatus = SyncStatus.PENDING_CREATE
        )

        applicationScope.launch(dispatcher + exceptionHandler) {
            createTransactionOnRemote(transactionEntity)
        }

        return safeDbCall(errorLogger) {
            transactionLocalDataSource.createTransaction(transactionEntity)
        }
    }

    /** Сразу получаем поток данных с БД и сразу запускаем синхронизацию на получение этих данныъ с сервера */
    override fun getTransactionsByAccountAndPeriod(
        accountId: String,
        startDate: String,
        endDate: String
    ): Flow<DomainResult<List<Transaction>>> {
        applicationScope.launch(dispatcher + exceptionHandler) {
            pullTransactionsFromRemoteForAccount(
                accountLocalId = accountId,
                startDate = startDate,
                endDate = endDate
            )
        }

        return safeDbFlow(errorLogger) {
            transactionLocalDataSource.getTransactionsByAccountAndPeriod(
                accountId = accountId,
                startDate = startDate,
                endDate = endDate
            ).map { list ->
                list.filter { it.syncStatus != SyncStatus.PENDING_DELETE }.map { it.toDomain() }
            }
        }
    }

    /**
     * 1. Получаем транзакцию из локальной БД (источник истины)
     * 2. Возвращаем ее сразу (offline-first)
     * 3. В фоне:
     *    - если есть serverId → обновляем с сервера
     *    - если нет → пытаемся создать на сервере
     */
    override suspend fun getTransactionById(localId: String): DomainResult<Transaction> {
        val resultDb = getLocalTransactionOrFail(localId)
        if (resultDb is DomainResult.Failure) return resultDb

        val transactionEntity = (resultDb as DomainResult.Success).data

        applicationScope.launch(dispatcher + exceptionHandler) {
            val serverId = transactionEntity.serverId
            if (serverId != null) {
                safeApiCall(errorLogger) {
                    transactionRemoteDataSource.getTransaction(serverId)
                }.onSuccess { transactionDto ->
                    updateLocalTransaction(
                        transactionDto.toEntity(
                            localId = transactionEntity.localId,
                            accountLocalId = transactionEntity.accountLocalId,
                            syncStatus = SyncStatus.SYNCED
                        )
                    )
                }
            } else {
                /** транзакция не синхронизирована с сервером то создаем на сервере*/
                createTransactionOnRemote(transactionEntity)
            }
        }

        return DomainResult.Success(transactionEntity.toDomain())
    }

    /** Вытаскиваем из бд транзакцию(так как только в БД храним serverId) обновляем локальныую трнакцию и
     * запускаем синхронизацию если транзакция не синхронизирована с сервером то создаем на сервере и
     * обновляем локально, иначе просто обновляем на сервере,
     * сразу возвращаем результат локального обновления транзакции */
    override suspend fun updateTransaction(transaction: Transaction): DomainResult<Unit> {
        val resultDb = getLocalTransactionOrFail(transaction.id)
        if (resultDb is DomainResult.Failure) return resultDb

        val transactionEntity = (resultDb as DomainResult.Success).data

        applicationScope.launch(dispatcher + exceptionHandler) {
            if (transactionEntity.serverId == null) {
                createTransactionOnRemote(
                    transaction.toEntity(
                        serverId = null,
                        accountServerId = transactionEntity.accountServerId,
                        syncStatus = SyncStatus.PENDING_UPDATE
                    )
                )
            } else {
                updateTransactionOnRemote(
                    transaction.toEntity(
                        serverId = transactionEntity.serverId,
                        accountServerId = transactionEntity.accountServerId,
                        syncStatus = SyncStatus.PENDING_UPDATE
                    )
                )
            }
        }

        return safeDbCall(errorLogger) {
            transactionLocalDataSource.updateTransaction(
                transactionEntity.copy(
                    categoryId = transaction.categoryId,
                    currencyCode = transaction.currencyCode,
                    amount = transaction.amount.toPlainString(),
                    transactionDate = DateHelper.dataTimeForApi(transaction.transactionDate),
                    comment = transaction.comment.orEmpty(),
                    createdAt = DateHelper.dataTimeForApi(transaction.createdAt),
                    updatedAt = DateHelper.dataTimeForApi(transaction.updatedAt),
                    syncStatus = if (transactionEntity.serverId == null)
                        SyncStatus.PENDING_CREATE
                    else
                        SyncStatus.PENDING_UPDATE
                )
            )
        }
    }

    override suspend fun deleteTransaction(id: String): DomainResult<Unit> {
        val localResult = getLocalTransactionOrFail(id)
        if (localResult is DomainResult.Failure) return localResult

        val transactionEntity = (localResult as DomainResult.Success).data

        applicationScope.launch(dispatcher + exceptionHandler) {
            deleteTransactionOnRemote(transactionEntity)
        }

        return safeDbCall(errorLogger) {
            transactionLocalDataSource.updateTransaction(
                transactionEntity.copy(syncStatus = SyncStatus.PENDING_DELETE)
            )
        }
    }

    /**  Хелпер для получения локальной транзакции*/
    private suspend fun getLocalTransactionOrFail(id: String): DomainResult<TransactionEntity> {
        return safeDbCall(errorLogger) {
            transactionLocalDataSource.getTransactionByLocalId(id)
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


    ////////////////// Sync //////////////////

    override suspend fun syncWith(synchronizer: Synchronizer): Boolean {
        return runCatching { performFullSync() }.isSuccess
    }

    private suspend fun performFullSync() {
        pullTransactionsFromRemoteForAllAccounts()
        pushPendingLocalTransactions()
    }

    /** Получаем все локальные акаунты и тянем с сервера все транзакции с момента создания аккаунта
     * и до сегодняшнего дня */
    private suspend fun pullTransactionsFromRemoteForAllAccounts() {
        accountLocalDataSource.getAccounts().first().forEach { account ->
            pullTransactionsFromRemoteForAccount(
                accountLocalId = account.localId,
                startDate = DateHelper.formatApiDate(account.createdAt),
                endDate = DateHelper.dateToApiFormat(DateHelper.getToday())
            )
        }
    }

    /** Получаем все локальные данныве которые ожидают синхронизацю и запускаем синхронизацию */
    private suspend fun pushPendingLocalTransactions() {
        safeDbCall(errorLogger) {
            transactionLocalDataSource.getPendingSync()
        }.onSuccess { transactionEntities ->
            transactionEntities.forEach { transactionEntity ->
                when (transactionEntity.syncStatus) {
                    SyncStatus.PENDING_CREATE -> createTransactionOnRemote(transactionEntity)
                    SyncStatus.PENDING_UPDATE -> updateTransactionOnRemote(transactionEntity)
                    SyncStatus.PENDING_DELETE -> deleteTransactionOnRemote(transactionEntity)
                    SyncStatus.SYNCED -> Unit
                }
            }
        }
    }

    /** Создаем на серевер транзакцию и обновляем локальную БД */
    private suspend fun createTransactionOnRemote(
        transactionEntity: TransactionEntity
    ) {
        transactionEntity.accountServerId?.let { accountServerId ->
            safeApiCall(errorLogger) {
                transactionRemoteDataSource.createTransaction(transactionEntity.toDto(accountServerId))
            }.onSuccess { transactionRequestDto ->
                updateLocalTransaction(
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
    private suspend fun updateTransactionOnRemote(transactionEntity: TransactionEntity) {
        transactionEntity.serverId?.let { serverId ->
            transactionEntity.accountServerId?.let { accountServerId ->
                safeApiCall(errorLogger) {
                    transactionRemoteDataSource.updateTransaction(
                        id = serverId,
                        transaction = transactionEntity.toDto(accountServerId)
                    )
                }.onSuccess { transactionDto ->
                    updateLocalTransaction(
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
    private suspend fun deleteTransactionOnRemote(transactionEntity: TransactionEntity) {
        if (transactionEntity.serverId == null) {
            deleteLocalTransaction(transactionEntity.localId)
        } else {
            safeApiCall(errorLogger) {
                transactionRemoteDataSource.delete(transactionEntity.serverId!!)
            }.onSuccess {
                deleteLocalTransaction(transactionEntity.localId)
            }
        }
    }

    private suspend fun deleteLocalTransaction(localId: String) {
        safeDbCall(errorLogger) {
            transactionLocalDataSource.deleteTransaction(localId)
        }
    }

    /** Унифицируем обновление локальной транзакции */
    private suspend fun updateLocalTransaction(transactionEntity: TransactionEntity) {
        safeDbCall(errorLogger) {
            transactionLocalDataSource.updateTransaction(transactionEntity)
        }
    }

    private suspend fun pullTransactionsFromRemoteForAccount(
        accountLocalId: String,
        startDate: String,
        endDate: String
    ) {
        // если аккаунт ещё не синхронизирован — транзакции с сервера не тянем
        val serverAccountId = getServerAccountIdByLocalId(accountLocalId) ?: return

        safeApiCall(errorLogger) {
            transactionRemoteDataSource.getTransactionsByAccountAndPeriod(
                serverAccountId,
                startDate,
                endDate
            )
        }.onSuccess { transactionDtos ->
            val serverIds = transactionDtos.map { it.id }

            val localTransactions = safeDbCall(errorLogger) {
                transactionLocalDataSource.getTransactionsByServerIds(serverIds)
            }.getOrNull().orEmpty()

            val localMap = localTransactions.associateBy { it.serverId }

            transactionDtos.forEach { transactionDto ->
                val localTransaction = localMap[transactionDto.id]

                if (localTransaction == null) {
                    /**  Локальной транзакции нет → создаём */
                    safeDbCall(errorLogger) {
                        transactionLocalDataSource.createTransaction(
                            transactionDto.toEntity(
                                localId = generateUUID(),
                                accountLocalId = accountLocalId,
                                syncStatus = SyncStatus.SYNCED
                            )
                        )
                    }
                } else if (transactionDto.updatedAt > localTransaction.updatedAt) {
                    /** Если есть то разрешаем конфликт, побежадает, так которая менялась позже  */
                    updateLocalTransaction(
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

    private suspend fun getServerAccountIdByLocalId(id: String): Int? {
        return safeDbCall(errorLogger) {
            accountLocalDataSource.getByLocalId(id)
        }.getOrNull()?.serverId
    }
}
