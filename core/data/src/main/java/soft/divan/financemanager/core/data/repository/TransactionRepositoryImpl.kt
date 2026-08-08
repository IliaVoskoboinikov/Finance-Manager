package soft.divan.financemanager.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import soft.divan.financemanager.core.data.TransactionRunner
import soft.divan.financemanager.core.data.error.DataError
import soft.divan.financemanager.core.data.mapper.ApiDateMapper
import soft.divan.financemanager.core.data.mapper.TimeMapper
import soft.divan.financemanager.core.data.mapper.toDomain
import soft.divan.financemanager.core.data.mapper.toDomainError
import soft.divan.financemanager.core.data.mapper.toDto
import soft.divan.financemanager.core.data.mapper.toEntity
import soft.divan.financemanager.core.data.mapper.toUpdateDto
import soft.divan.financemanager.core.data.outbox.OutboxEnqueuer
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.financemanager.core.data.source.CategoryLocalDataSource
import soft.divan.financemanager.core.data.source.TransactionLocalDataSource
import soft.divan.financemanager.core.data.source.TransactionRemoteDataSource
import soft.divan.financemanager.core.data.sync.TransactionSyncManager
import soft.divan.financemanager.core.data.util.coroutne.AppCoroutineContext
import soft.divan.financemanager.core.data.util.safeCall.safeApiCall
import soft.divan.financemanager.core.data.util.safeCall.safeDbCall
import soft.divan.financemanager.core.data.util.safeCall.safeDbFlow
import soft.divan.financemanager.core.database.entity.TransactionEntity
import soft.divan.financemanager.core.database.model.OutboxEntityType
import soft.divan.financemanager.core.database.model.OutboxOperation
import soft.divan.financemanager.core.database.model.SyncStatus
import soft.divan.financemanager.core.domain.model.Transaction
import soft.divan.financemanager.core.domain.model.TransactionType
import soft.divan.financemanager.core.domain.repository.TransactionRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.domain.result.fold
import soft.divan.financemanager.core.domain.result.onSuccess
import soft.divan.financemanager.core.loggingerror.ErrorLogger
import java.time.Instant
import javax.inject.Inject

@Suppress("LongParameterList")
class TransactionRepositoryImpl @Inject constructor(
    private val remoteDataSource: TransactionRemoteDataSource,
    private val localDataSource: TransactionLocalDataSource,
    private val accountLocalDataSource: AccountLocalDataSource,
    private val categoryLocalDataSource: CategoryLocalDataSource,
    private val syncManager: TransactionSyncManager,
    private val transactionRunner: TransactionRunner,
    private val outboxEnqueuer: OutboxEnqueuer,
    private val appCoroutineContext: AppCoroutineContext,
    private val errorLogger: ErrorLogger
) : TransactionRepository {

    /** Сохраняет транзакцию локально и ставит её создание в очередь исходящих операций. */
    override suspend fun create(transaction: Transaction): DomainResult<Unit> {
        val transactionEntity = transaction.toEntity(
            serverId = null,
            accountServerId = accountLocalDataSource.getByLocalId(transaction.accountLocalId)?.serverId,
            syncStatus = SyncStatus.PENDING_CREATE
        )

        return transactionRunner.runInTransaction {
            safeDbCall(errorLogger) {
                localDataSource.insert(transactionEntity)
                outboxEnqueuer.enqueue(
                    entityType = OutboxEntityType.TRANSACTION,
                    entityLocalId = transactionEntity.localId,
                    operation = OutboxOperation.CREATE,
                    body = transactionEntity.toDto(transactionEntity.accountSyncId())
                )
                Unit
            }
        }
    }

    /** Сразу получаем поток данных с БД и сразу запускаем синхронизацию на получение этих данных с сервера */
    override fun getByAccountAndPeriod(
        accountId: String,
        startDate: Instant,
        endDate: Instant
    ): Flow<DomainResult<List<Transaction>>> {
        val startDateStr = ApiDateMapper.toApiDate(startDate)
        val endDateStr = ApiDateMapper.toApiDate(endDate)

        appCoroutineContext.launch {
            syncManager.pullFromRemoteForAccount(
                accountLocalId = accountId,
                startDate = startDateStr,
                endDate = endDateStr
            )
        }

        return safeDbFlow(errorLogger) {
            localDataSource.getByAccountAndPeriod(
                accountId = accountId,
                startDate = startDateStr,
                endDate = endDateStr
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

        // Несинхронизированную транзакцию догонять не нужно: её создание уже стоит в очереди
        val serverId = transactionEntity.serverId
        if (serverId != null) {
            appCoroutineContext.launchSync {
                safeApiCall(errorLogger) {
                    remoteDataSource.get(serverId)
                }.onSuccess { transactionDto ->
                    val category = categoryLocalDataSource.getById(
                        transactionDto.categoryId
                    ) ?: return@onSuccess
                    val type = if (category.isIncome) TransactionType.INCOME else TransactionType.EXPENSE

                    safeDbCall(errorLogger) {
                        localDataSource.update(
                            transactionDto.toEntity(
                                localId = transactionEntity.localId,
                                accountLocalId = transactionEntity.accountLocalId,
                                currencyId = transactionEntity.currencyId,
                                type = type,
                                syncStatus = SyncStatus.SYNCED
                            )
                        )
                    }
                }
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
        val updatedEntity = transactionEntity.copy(
            categoryId = transaction.categoryId,
            currencyId = transaction.currencyId,
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

        return transactionRunner.runInTransaction {
            safeDbCall(errorLogger) {
                localDataSource.update(updatedEntity)
                outboxEnqueuer.enqueue(
                    entityType = OutboxEntityType.TRANSACTION,
                    entityLocalId = updatedEntity.localId,
                    operation = OutboxOperation.UPDATE,
                    targetServerId = updatedEntity.syncId(),
                    body = updatedEntity.toUpdateDto(updatedEntity.accountSyncId())
                )
                Unit
            }
        }
    }

    override suspend fun delete(id: String): DomainResult<Unit> {
        val localResult = getLocalOrFail(id)
        if (localResult is DomainResult.Failure) return localResult

        val transactionEntity = (localResult as DomainResult.Success).data

        return transactionRunner.runInTransaction {
            safeDbCall(errorLogger) {
                localDataSource.update(
                    transactionEntity.copy(syncStatus = SyncStatus.PENDING_DELETE)
                )
                outboxEnqueuer.enqueue(
                    entityType = OutboxEntityType.TRANSACTION,
                    entityLocalId = transactionEntity.localId,
                    operation = OutboxOperation.DELETE,
                    targetServerId = transactionEntity.syncId()
                )
                Unit
            }
        }
    }

    /**
     * Идентификатор, под которым транзакция известна серверу.
     *
     * Пока создание не подтверждено, `serverId` ещё не проставлен — но сервер узнает запись по
     * клиентскому `localId`, с которым ушёл `POST`. Строгий порядок очереди гарантирует, что
     * создание уедет раньше последующих правок, поэтому адресовать их можно уже сейчас.
     */
    private fun TransactionEntity.syncId(): String = serverId ?: localId

    /** То же для родительского счёта: до подтверждения он известен серверу под своим `localId`. */
    private fun TransactionEntity.accountSyncId(): String = accountServerId ?: accountLocalId

    /**
     * Есть ли у счёта хотя бы одна операция. Проверяется по локальной БД (SSOT) тем же путём,
     * что и логика удаления счёта в `AccountRepositoryImpl.delete`, чтобы предупреждение в UI
     * совпадало с реальным поведением (архивирование против физического удаления).
     */
    override suspend fun hasTransactions(accountId: String): DomainResult<Boolean> {
        return safeDbCall(errorLogger) {
            localDataSource.getByAccountId(accountId).isNotEmpty()
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
