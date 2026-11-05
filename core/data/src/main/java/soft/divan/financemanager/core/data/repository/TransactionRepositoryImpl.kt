package soft.divan.financemanager.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import soft.divan.financemanager.core.data.transaction.TransactionRunner
import soft.divan.financemanager.core.data.transaction.rollbackOnError
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
import soft.divan.financemanager.core.data.source.OutboxLocalDataSource
import soft.divan.financemanager.core.data.source.TransactionLocalDataSource
import soft.divan.financemanager.core.data.source.TransactionRemoteDataSource
import soft.divan.financemanager.core.data.sync.TransactionSyncManager
import soft.divan.financemanager.core.data.util.coroutine.AppCoroutineContext
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
import soft.divan.financemanager.core.domain.result.getOrNull
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
    private val outboxLocalDataSource: OutboxLocalDataSource,
    private val appCoroutineContext: AppCoroutineContext,
    private val errorLogger: ErrorLogger
) : TransactionRepository {

    /** Сохраняет транзакцию локально и ставит её создание в очередь исходящих операций. */
    override suspend fun create(transaction: Transaction): DomainResult<Unit> {
        val transactionEntity = transaction.toEntity(
            serverId = null,
            syncStatus = SyncStatus.PENDING_CREATE
        )

        return transactionRunner.runInTransaction {
            val accountSyncId =
                getAccountSyncIdOrFail(transactionEntity.accountLocalId).rollbackOnError()

            safeDbCall(errorLogger) {
                localDataSource.insert(transactionEntity)
                outboxEnqueuer.enqueue(
                    entityType = OutboxEntityType.TRANSACTION,
                    entityLocalId = transactionEntity.localId,
                    // Предшественник транзакции — её счёт: сервер отвергнет её с неизвестным
                    // accountId, поэтому она обязана уехать после создания счёта
                    dependencyKey = transactionEntity.accountLocalId,
                    operation = OutboxOperation.CREATE,
                    body = transactionEntity.toDto(accountSyncId)
                )
            }.rollbackOnError()
            // Без rollbackOnError транзакция закоммитила бы строку без операции в очереди:
            // safeDbCall гасит исключение, а откатывает её только исключение
            DomainResult.Success(Unit)
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
     * 3. В фоне, если у транзакции есть serverId, обновляем её с сервера (см. [refreshFromServer])
     */
    override suspend fun getById(localId: String): DomainResult<Transaction> {
        val resultDb = getLocalOrFail(localId)
        if (resultDb is DomainResult.Failure) return resultDb

        val transactionEntity = (resultDb as DomainResult.Success).data

        // Несинхронизированную транзакцию догонять не нужно: её создание уже стоит в очереди
        val serverId = transactionEntity.serverId
        if (serverId != null) {
            appCoroutineContext.launchSync {
                refreshFromServer(transactionEntity, serverId)
            }
        }

        return DomainResult.Success(transactionEntity.toDomain())
    }

    /**
     * Заменяет локальную транзакцию серверной версией, если её можно принять ([refreshableRow]).
     *
     * До запроса строка запоминается — потому что `getById` зовут и use case'ы записи: обновление
     * стартует после их commit, когда операция ещё в очереди, и ответ сервера заведомо не знает о
     * ней. Ответ пишется, только если в транзакции записи строка всё та же: за время запроса её
     * могли изменить пользователь (и правка успеть уехать) или pull, и ответ, ушедший с сервера
     * раньше, откатил бы их. `updatedAt` при этом не сравнивается: после `PUT` он остаётся по
     * часам клиента.
     */
    private suspend fun refreshFromServer(local: TransactionEntity, serverId: String) {
        val snapshot =
            safeDbCall(errorLogger) { refreshableRow(local.localId) }.getOrNull() ?: return

        safeApiCall(errorLogger) {
            remoteDataSource.get(serverId)
        }.onSuccess { transactionDto ->
            val category = categoryLocalDataSource.getById(
                transactionDto.categoryId
            ) ?: return@onSuccess
            val type = if (category.isIncome) TransactionType.INCOME else TransactionType.EXPENSE

            safeDbCall(errorLogger) {
                transactionRunner.runInTransaction {
                    // Счёт берётся из серверной версии: другое устройство могло перенести
                    // операцию, и со старым счётом следующая правка вернула бы перенос на
                    // сервере. Валюта операции — валюта её счёта, как и при pull; читается в
                    // той же транзакции, что и запись, чтобы не взять валюту до правки счёта
                    val account = accountLocalDataSource.getByServerId(transactionDto.accountId)
                    if (account != null && refreshableRow(local.localId) == snapshot) {
                        localDataSource.update(
                            transactionDto.toEntity(
                                localId = local.localId,
                                accountLocalId = account.localId,
                                currencyId = account.currencyId,
                                type = type,
                                syncStatus = SyncStatus.SYNCED
                            )
                        )
                    }
                }
            }
        }
    }

    /**
     * Строка транзакции, которую можно заменить серверной версией, или `null`: строка
     * синхронизирована и в очереди нет её операций. Иначе ответ сервера откатил бы ещё не
     * отправленную правку, а `PENDING_DELETE` сменился бы на `SYNCED`, вернув удалённую транзакцию
     * в списки.
     */
    private suspend fun refreshableRow(localId: String): TransactionEntity? =
        localDataSource.getByLocalId(localId)?.takeIf {
            it.syncStatus == SyncStatus.SYNCED &&
                !outboxLocalDataSource.hasOpenOperationsAffecting(localId)
        }

    /**
     * Сохраняет правку локально и ставит `PUT` в очередь исходящих операций.
     *
     * Строка берётся из БД — только там хранится `serverId`. Если правка переносит операцию на
     * другой счёт, вместе с ним меняются предшественник в очереди (`dependencyKey`) и `accountId`
     * в теле запроса: правка уедет после создания нового счёта, и сервер переложит сумму со
     * старого счёта на новый. Старый счёт уходит в `formerDependencyKey` — до отправки правки он
     * считается затронутым ею.
     */
    override suspend fun update(transaction: Transaction): DomainResult<Unit> {
        val resultDb = getLocalOrFail(transaction.id)
        if (resultDb is DomainResult.Failure) return resultDb

        val transactionEntity = (resultDb as DomainResult.Success).data

        val formerAccountLocalId = transactionEntity.accountLocalId
            .takeIf { it != transaction.accountLocalId }

        return transactionRunner.runInTransaction {
            val accountSyncId = getAccountSyncIdOrFail(transaction.accountLocalId).rollbackOnError()

            safeDbCall(errorLogger) {
                val updatedEntity = transactionEntity.copy(
                    accountLocalId = transaction.accountLocalId,
                    type = transaction.type.name,
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

                localDataSource.update(updatedEntity)
                outboxEnqueuer.enqueue(
                    entityType = OutboxEntityType.TRANSACTION,
                    entityLocalId = updatedEntity.localId,
                    dependencyKey = updatedEntity.accountLocalId,
                    operation = OutboxOperation.UPDATE,
                    targetServerId = updatedEntity.syncId(),
                    body = updatedEntity.toUpdateDto(accountSyncId),
                    // Пока перенос не уехал, сервер держит операцию на старом счёте: его
                    // удаление и серверная версия баланса должны дождаться этого PUT
                    formerDependencyKey = formerAccountLocalId
                )
            }.rollbackOnError()
            DomainResult.Success(Unit)
        }
    }

    /**
     * Id, под которым сервер знает счёт операции сейчас: `serverId`, а до подтверждения создания —
     * клиентский `localId`, с которым ушёл `POST` (барьер порядка не выпустит операцию раньше
     * него). Читается из строки счёта при каждой постановке в очередь: копия в строке транзакции
     * устарела бы, как только счёт получит `serverId`.
     *
     * Счёта нет локально — `NoData`: иначе запрос ушёл бы с неизвестным серверу `accountId` и
     * осел бы в dead-letter.
     */
    private suspend fun getAccountSyncIdOrFail(accountLocalId: String): DomainResult<String> {
        return safeDbCall(errorLogger) {
            accountLocalDataSource.getByLocalId(accountLocalId)
        }.fold(
            onSuccess = { account ->
                account?.let { DomainResult.Success(it.serverId ?: it.localId) }
                    ?: DomainResult.Failure(DataError.NotFound.toDomainError())
            },
            onFailure = { error -> DomainResult.Failure(error) }
        )
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
                    dependencyKey = transactionEntity.accountLocalId,
                    operation = OutboxOperation.DELETE,
                    targetServerId = transactionEntity.syncId()
                )
            }.rollbackOnError()
            DomainResult.Success(Unit)
        }
    }

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

/**
 * Идентификатор, под которым транзакция известна серверу.
 *
 * Пока создание не подтверждено, `serverId` ещё не проставлен — но сервер узнает запись по
 * клиентскому `localId`, с которым ушёл `POST`. Адресовать правку можно уже сейчас, потому
 * что барьер порядка по `dependencyKey` не выпустит её раньше создания той же сущности —
 * даже если создание ушло в backoff или удерживает аренду.
 */
private fun TransactionEntity.syncId(): String = serverId ?: localId
