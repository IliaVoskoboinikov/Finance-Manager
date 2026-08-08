package soft.divan.financemanager.core.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import soft.divan.financemanager.core.data.TransactionRunner
import soft.divan.financemanager.core.data.error.DataError
import soft.divan.financemanager.core.data.mapper.TimeMapper
import soft.divan.financemanager.core.data.mapper.toDomain
import soft.divan.financemanager.core.data.mapper.toDomainError
import soft.divan.financemanager.core.data.mapper.toDto
import soft.divan.financemanager.core.data.mapper.toEntity
import soft.divan.financemanager.core.data.mapper.toUpdateDto
import soft.divan.financemanager.core.data.outbox.OutboxEnqueuer
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.financemanager.core.data.source.AccountRemoteDataSource
import soft.divan.financemanager.core.data.source.TransactionLocalDataSource
import soft.divan.financemanager.core.data.sync.AccountSyncManager
import soft.divan.financemanager.core.data.util.coroutne.AppCoroutineContext
import soft.divan.financemanager.core.data.util.safeCall.safeApiCall
import soft.divan.financemanager.core.data.util.safeCall.safeDbCall
import soft.divan.financemanager.core.data.util.safeCall.safeDbFlow
import soft.divan.financemanager.core.database.entity.AccountEntity
import soft.divan.financemanager.core.database.model.OutboxEntityType
import soft.divan.financemanager.core.database.model.OutboxOperation
import soft.divan.financemanager.core.database.model.SyncStatus
import soft.divan.financemanager.core.domain.model.Account
import soft.divan.financemanager.core.domain.model.AccountStatus
import soft.divan.financemanager.core.domain.repository.AccountRepository
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.domain.result.fold
import soft.divan.financemanager.core.domain.result.onSuccess
import soft.divan.financemanager.core.loggingerror.ErrorLogger
import java.math.BigDecimal
import javax.inject.Inject

@Suppress("LongParameterList")
class AccountRepositoryImpl @Inject constructor(
    private val remoteDataSource: AccountRemoteDataSource,
    private val localDataSource: AccountLocalDataSource,
    private val transactionLocalDataSource: TransactionLocalDataSource,
    private val syncManager: AccountSyncManager,
    private val transactionRunner: TransactionRunner,
    private val outboxEnqueuer: OutboxEnqueuer,
    private val appCoroutineContext: AppCoroutineContext,
    private val errorLogger: ErrorLogger
) : AccountRepository {

    /** Сохраняет счёт локально и ставит его создание в очередь исходящих операций. */
    override suspend fun create(account: Account): DomainResult<Unit> {
        val accountEntity = account.toEntity(serverId = null, syncStatus = SyncStatus.PENDING_CREATE)

        return transactionRunner.runInTransaction {
            safeDbCall(errorLogger) {
                localDataSource.create(accountEntity)
                outboxEnqueuer.enqueue(
                    entityType = OutboxEntityType.ACCOUNT,
                    entityLocalId = accountEntity.localId,
                    operation = OutboxOperation.CREATE,
                    body = accountEntity.toDto()
                )
                Unit
            }
        }
    }

    /** Сразу получаем поток данных с БД и сразу запускаем синхронизацию */
    override fun getAll(): Flow<DomainResult<List<Account>>> {
        appCoroutineContext.launch {
            syncManager.pullServerData()
        }
        return safeDbFlow(errorLogger) {
            localDataSource.getAll().map { list ->
                list
                    .filter { it.syncStatus != SyncStatus.PENDING_DELETE }
                    .map { it.toDomain() }
                    .filter { it.status != AccountStatus.Deleted }
            }
        }
    }

    /**
     * 1. Получаем аккаунт из локальной БД (источник истины)
     * 2. Возвращаем его сразу (offline-first)
     * 3. В фоне:
     *    - если есть serverId → обновляем с сервера
     *    - если нет → пытаемся создать на сервере
     */
    override suspend fun getById(id: String): DomainResult<Account> {
        val localResult = getLocalOrFail(id)
        if (localResult is DomainResult.Failure) return localResult

        val accountEntity = (localResult as DomainResult.Success).data

        // Несинхронизированный счёт догонять не нужно: его создание уже стоит в очереди
        val serverId = accountEntity.serverId
        if (serverId != null) {
            appCoroutineContext.launchSync {
                safeApiCall(errorLogger) {
                    remoteDataSource.getById(serverId)
                }.onSuccess { accountDto ->
                    safeDbCall(errorLogger) {
                        localDataSource.update(
                            accountDto.toEntity(
                                localId = accountEntity.localId,
                                syncStatus = SyncStatus.SYNCED
                            )
                        )
                    }
                }
            }
        }

        return DomainResult.Success(accountEntity.toDomain())
    }

    /** Вытаскиваем из бд аккаунт(так как только в БД храним serverId) обновляем локальный аккаунт и
     * запускаем синхронизацию если аккаунт не синхронизирован с сервером то создаем на сервере и
     * обновляем локально, иначе просто обновляем на сервере */
    override suspend fun update(account: Account): DomainResult<Unit> {
        val resultDb = getLocalOrFail(account.id)
        if (resultDb is DomainResult.Failure) return resultDb

        val accountEntity = (resultDb as DomainResult.Success).data
        val updatedEntity = accountEntity.copy(
            name = account.name,
            balance = account.balance.toPlainString(),
            currencyId = account.currencyId,
            createdAt = TimeMapper.toApi(account.createdAt),
            updatedAt = TimeMapper.toApi(account.updatedAt),
            syncStatus = if (accountEntity.serverId == null) {
                SyncStatus.PENDING_CREATE
            } else {
                SyncStatus.PENDING_UPDATE
            }
        )

        return transactionRunner.runInTransaction {
            safeDbCall(errorLogger) {
                localDataSource.update(updatedEntity)
                outboxEnqueuer.enqueue(
                    entityType = OutboxEntityType.ACCOUNT,
                    entityLocalId = updatedEntity.localId,
                    operation = OutboxOperation.UPDATE,
                    targetServerId = updatedEntity.syncId(),
                    body = updatedEntity.toUpdateDto()
                )
                Unit
            }
        }
    }

    /**
     * Обновляет только баланс счёта локально: без пуша на сервер и без смены syncStatus/updatedAt.
     *
     * Вызывается из транзакционных use case'ов (создание/изменение/удаление транзакции):
     * сервер сам пересчитывает баланс при пуше транзакции, а PUT /account с балансом
     * привёл бы к двойному применению суммы. updatedAt намеренно не трогаем, чтобы
     * серверная версия счёта (обновлённая сервером после пуша транзакции) гарантированно
     * выигрывала last-write-wins при следующем pull.
     */
    override suspend fun updateBalanceLocal(
        accountId: String,
        balance: BigDecimal
    ): DomainResult<Unit> {
        val resultDb = getLocalOrFail(accountId)
        if (resultDb is DomainResult.Failure) return resultDb

        val accountEntity = (resultDb as DomainResult.Success).data

        return safeDbCall(errorLogger) {
            localDataSource.update(accountEntity.copy(balance = balance.toPlainString()))
        }
    }

    /**
     * Удаляет счёт с учётом серверного правила «нельзя удалить счёт, на котором есть операции».
     *
     * В обоих случаях на сервер уходит один и тот же запрос — `DELETE /accounts/{id}` (сервер сам
     * решает: нет операций → физическое удаление, есть → перевод в статус `Deleted`). Различается
     * только локальное отражение результата:
     * - операций нет → строка помечается [SyncStatus.PENDING_DELETE] и после успешного серверного
     *   удаления удаляется локально;
     * - операции есть → строка переводится в статус `Deleted` (плюс [SyncStatus.PENDING_DELETE] как
     *   признак «ждём подтверждения сервера»): счёт пропадает из списков/пикера ([getAll]), но
     *   остаётся в БД для истории. После успешного серверного DELETE строка сохраняется как архивная.
     *
     * Статус `Deleted` в самой записи и определяет, оставлять её или удалять после ответа сервера
     * (см. [AccountSyncManager.syncDelete]). Обратной раз-архивации нет.
     */
    @Suppress("ReturnCount")
    override suspend fun delete(id: String): DomainResult<Unit> {
        val localResult = getLocalOrFail(id)

        if (localResult is DomainResult.Failure) return localResult

        val accountEntity = (localResult as DomainResult.Success).data

        val hasTransactions = safeDbCall(errorLogger) {
            transactionLocalDataSource.getByAccountId(id).isNotEmpty()
        }
        if (hasTransactions is DomainResult.Failure) return hasTransactions

        val shouldArchive = (hasTransactions as DomainResult.Success).data
        val markedEntity = accountEntity.copy(
            status = if (shouldArchive) AccountStatus.Deleted.name else accountEntity.status,
            syncStatus = SyncStatus.PENDING_DELETE
        )

        return transactionRunner.runInTransaction {
            safeDbCall(errorLogger) {
                localDataSource.update(markedEntity)
                outboxEnqueuer.enqueue(
                    entityType = OutboxEntityType.ACCOUNT,
                    entityLocalId = markedEntity.localId,
                    operation = OutboxOperation.DELETE,
                    targetServerId = markedEntity.syncId()
                )
                Unit
            }
        }
    }

    /**
     * Идентификатор, под которым счёт известен серверу.
     *
     * Пока создание не подтверждено, `serverId` ещё не проставлен — но сервер узнает счёт по
     * клиентскому `localId`, с которым ушёл `POST`. Строгий порядок очереди гарантирует, что
     * создание уедет раньше последующих правок, поэтому адресовать их можно уже сейчас.
     */
    private fun AccountEntity.syncId(): String = serverId ?: localId

    /**  Хелпер для получения локального аккаунта*/
    private suspend fun getLocalOrFail(id: String): DomainResult<AccountEntity> {
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
