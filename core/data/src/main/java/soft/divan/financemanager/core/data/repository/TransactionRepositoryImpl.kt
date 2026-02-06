package soft.divan.financemanager.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import soft.divan.financemanager.core.data.error.DataError
import soft.divan.financemanager.core.data.mapper.ApiDateMapper
import soft.divan.financemanager.core.data.mapper.TimeMapper
import soft.divan.financemanager.core.data.mapper.toDomain
import soft.divan.financemanager.core.data.mapper.toDomainError
import soft.divan.financemanager.core.data.mapper.toEntity
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.financemanager.core.data.source.TransactionLocalDataSource
import soft.divan.financemanager.core.data.source.TransactionRemoteDataSource
import soft.divan.financemanager.core.data.sync.TransactionSyncManager
import soft.divan.financemanager.core.data.util.coroutne.AppCoroutineContext
import soft.divan.financemanager.core.data.util.safeCall.safeApiCall
import soft.divan.financemanager.core.data.util.safeCall.safeDbCall
import soft.divan.financemanager.core.data.util.safeCall.safeDbFlow
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.repository.TransactionRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.domain.result.fold
import soft.divan.financemanager.core.domain.result.onSuccess
import soft.divan.financemanager.core.loggingerror.api.ErrorLogger
import soft.divan.finansemanager.core.database.entity.TransactionEntity
import soft.divan.finansemanager.core.database.model.SyncStatus
import java.time.Instant
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val remoteDataSource: TransactionRemoteDataSource,
    private val localDataSource: TransactionLocalDataSource,
    private val accountLocalDataSource: AccountLocalDataSource,
    private val syncManager: TransactionSyncManager,
    private val appCoroutineContext: AppCoroutineContext,
    private val errorLogger: ErrorLogger
) : TransactionRepository {

    /** Создаем транзакцию в БД и сразу запускаем синхронизацию */
    override suspend fun create(transaction: Transaction): DomainResult<Unit> {
        val accountServerId = accountLocalDataSource.getByLocalId(
            transaction.accountLocalId
        )?.serverId

        val transactionEntity = transaction.toEntity(
            serverId = null,
            accountServerId = accountServerId,
            syncStatus = SyncStatus.PENDING_CREATE
        )

        appCoroutineContext.launch {
            syncManager.syncCreate(transactionEntity)
        }

        return safeDbCall(errorLogger) {
            localDataSource.create(transactionEntity)
        }
    }

    /** Сразу получаем поток данных с БД и сразу запускаем синхронизацию на получение этих данныъ с сервера */
    override fun getByAccountAndPeriod(
        accountId: String,
        startDate: Instant,
        endDate: Instant
    ): Flow<DomainResult<List<Transaction>>> {
        val startDate = ApiDateMapper.toApiDate(startDate)
        val endDate = ApiDateMapper.toApiDate(endDate)
        appCoroutineContext.launch {
            syncManager.pullFromRemoteForAccount(
                accountLocalId = accountId,
                startDate = startDate,
                endDate = endDate
            )
        }

        return safeDbFlow(errorLogger) {
            localDataSource.getByAccountAndPeriod(
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
    override suspend fun getById(localId: String): DomainResult<Transaction> {
        val resultDb = getLocalOrFail(localId)
        if (resultDb is DomainResult.Failure) return resultDb

        val transactionEntity = (resultDb as DomainResult.Success).data

        appCoroutineContext.launch {
            val serverId = transactionEntity.serverId
            if (serverId != null) {
                safeApiCall(errorLogger) {
                    remoteDataSource.get(serverId)
                }.onSuccess { transactionDto ->
                    safeDbCall(errorLogger) {
                        localDataSource.update(
                            transactionDto.toEntity(
                                localId = transactionEntity.localId,
                                accountLocalId = transactionEntity.accountLocalId,
                                syncStatus = SyncStatus.SYNCED
                            )
                        )
                    }
                }
            } else {
                /** транзакция не синхронизирована с сервером то создаем на сервере*/
                syncManager.syncCreate(transactionEntity)
            }
        }

        return DomainResult.Success(transactionEntity.toDomain())
    }

    /** Вытаскиваем из бд транзакцию(так как только в БД храним serverId) обновляем локальныую трнакцию и
     * запускаем синхронизацию если транзакция не синхронизирована с сервером то создаем на сервере и
     * обновляем локально, иначе просто обновляем на сервере,
     * сразу возвращаем результат локального обновления транзакции */
    override suspend fun update(transaction: Transaction): DomainResult<Unit> {
        val resultDb = getLocalOrFail(transaction.id)
        if (resultDb is DomainResult.Failure) return resultDb

        val transactionEntity = (resultDb as DomainResult.Success).data

        appCoroutineContext.launch {
            if (transactionEntity.serverId == null) {
                syncManager.syncCreate(
                    transaction.toEntity(
                        serverId = null,
                        accountServerId = transactionEntity.accountServerId,
                        syncStatus = SyncStatus.PENDING_CREATE
                    )
                )
            } else {
                syncManager.syncUpdate(
                    transaction.toEntity(
                        serverId = transactionEntity.serverId,
                        accountServerId = transactionEntity.accountServerId,
                        syncStatus = SyncStatus.PENDING_UPDATE
                    )
                )
            }
        }

        return safeDbCall(errorLogger) {
            localDataSource.update(
                transactionEntity.copy(
                    categoryId = transaction.categoryId,
                    currencyCode = transaction.currencyCode,
                    amount = transaction.amount.toPlainString(),
                    transactionDate = TimeMapper.toApi(transaction.transactionDate),
                    comment = transaction.comment.orEmpty(),
                    createdAt = TimeMapper.toApi(transaction.createdAt),
                    updatedAt = TimeMapper.toApi(transaction.updatedAt),
                    syncStatus = if (transactionEntity.serverId == null) {
                        SyncStatus.PENDING_CREATE
                    } else {
                        SyncStatus.PENDING_UPDATE
                    }
                )
            )
        }
    }

    override suspend fun delete(id: String): DomainResult<Unit> {
        val localResult = getLocalOrFail(id)
        if (localResult is DomainResult.Failure) return localResult

        val transactionEntity = (localResult as DomainResult.Success).data

        appCoroutineContext.launch {
            syncManager.syncDelete(transactionEntity)
        }

        return safeDbCall(errorLogger) {
            localDataSource.update(
                transactionEntity.copy(syncStatus = SyncStatus.PENDING_DELETE)
            )
        }
    }

    /**  Хелпер для получения локальной транзакции*/
    private suspend fun getLocalOrFail(id: String): DomainResult<TransactionEntity> {
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
// Revue me>>
