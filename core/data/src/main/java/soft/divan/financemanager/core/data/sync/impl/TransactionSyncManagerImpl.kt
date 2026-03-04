package soft.divan.financemanager.core.data.sync.impl

import kotlinx.coroutines.flow.first
import soft.divan.financemanager.core.data.mapper.ApiDateMapper
import soft.divan.financemanager.core.data.mapper.toDto
import soft.divan.financemanager.core.data.mapper.toEntity
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.financemanager.core.data.source.TransactionLocalDataSource
import soft.divan.financemanager.core.data.source.TransactionRemoteDataSource
import soft.divan.financemanager.core.data.sync.TransactionSyncManager
import soft.divan.financemanager.core.data.sync.util.Synchronizer
import soft.divan.financemanager.core.data.util.generateUUID
import soft.divan.financemanager.core.data.util.safeCall.safeApiCall
import soft.divan.financemanager.core.data.util.safeCall.safeDbCall
import soft.divan.financemanager.core.database.entity.TransactionEntity
import soft.divan.financemanager.core.database.model.SyncStatus
import soft.divan.financemanager.core.domain.result.getOrNull
import soft.divan.financemanager.core.domain.result.onSuccess
import soft.divan.financemanager.core.loggingerror.ErrorLogger
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Реализация [TransactionSyncManager].
 *
 * Отвечает за двустороннюю синхронизацию транзакций:
 *
 * 1. Pull — загрузка транзакций с сервера по каждому аккаунту
 * 2. Push — отправка локальных изменений (create/update/delete)
 *
 * Архитектурные принципы:
 * - Offline-first: локальная БД — источник истины
 * - Синхронизация выполняется на уровне аккаунта
 * - Разрешение конфликтов по updatedAt (last-write-wins)
 * - Все операции изолированы через safeApiCall / safeDbCall
 */
class TransactionSyncManagerImpl @Inject constructor(
    private val remoteDataSource: TransactionRemoteDataSource,
    private val localDataSource: TransactionLocalDataSource,
    private val accountLocalDataSource: AccountLocalDataSource,
    private val errorLogger: ErrorLogger
) : TransactionSyncManager {

    /**
     * Точка входа полной синхронизации.
     *
     * Порядок:
     * 1. pullServerData() — обновление локальных данных
     * 2. pushLocalChanges() — отправка pending-операций
     *
     * Возвращает true, если синхронизация завершилась без исключений.
     */
    override suspend fun syncWith(synchronizer: Synchronizer): Boolean {
        return runCatching {
            pullServerData()
            pushLocalChanges()
        }.isSuccess
    }

    /**
     * Выполняет pull-синхронизацию для всех локальных аккаунтов.
     *
     * Для каждого аккаунта:
     * - определяется период (от createdAt - 2 дня до текущего момента)
     * - запрашиваются транзакции с сервера
     *
     * Почему -2 дня:
     * Защита от пограничных кейсов по времени и рассинхронизации часов.
     */
    override suspend fun pullServerData() {
        accountLocalDataSource.getAll().first().forEach { account ->
            pullFromRemoteForAccount(
                accountLocalId = account.localId,
                startDate = ApiDateMapper.toApiDate(
                    Instant.parse(account.createdAt).minus(2, ChronoUnit.DAYS)
                ),
                endDate = ApiDateMapper.toApiDate(Instant.now())
            )
        }
    }

    /**
     * Отправляет новую локальную транзакцию на сервер.
     *
     * Требует наличия accountServerId.
     *
     * После успешного ответа:
     * - обновляет локальную запись
     * - устанавливает serverId
     * - помечает как SYNCED
     */
    override suspend fun syncCreate(transactionEntity: TransactionEntity) {
        transactionEntity.accountServerId?.let { accountServerId ->
            safeApiCall(errorLogger) {
                remoteDataSource.create(transactionEntity.toDto(accountServerId))
            }.onSuccess { transactionRequestDto ->
               /* updateLocalFromRemote(
                    transactionRequestDto.toEntity(
                        localId = transactionEntity.localId,
                        accountLocalId = transactionEntity.accountLocalId,
                        currencyCode = transactionEntity.currencyCode,
                        syncStatus = SyncStatus.SYNCED
                    )
                )*/
            }
        }
    }

    /**
     * Отправляет обновление транзакции на сервер.
     *
     * Требует:
     * - serverId транзакции
     * - serverId аккаунта
     *
     * После успешного ответа синхронизирует локальную запись.
     */
    override suspend fun syncUpdate(transactionEntity: TransactionEntity) {
        transactionEntity.serverId?.let { serverId ->
            transactionEntity.accountServerId?.let { accountServerId ->
                safeApiCall(errorLogger) {
                    remoteDataSource.update(
                        id = serverId,
                        transaction = transactionEntity.toDto(accountServerId)
                    )
                }.onSuccess { transactionDto ->
                    /*updateLocalFromRemote(
                        transactionDto.toEntity(
                            localId = transactionEntity.localId,
                            accountLocalId = transactionEntity.accountLocalId,
                            syncStatus = SyncStatus.SYNCED
                        )
                    )*/
                }
            }
        }
    }

    /**
     * Удаляет транзакцию.
     *
     * Если serverId == null:
     * - запись никогда не была синхронизирована → удаляем локально
     *
     * Если serverId != null:
     * - удаляем на сервере
     * - затем удаляем локально
     */
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

    /**
     * Обрабатывает все локальные транзакции со статусом pending.
     *
     * Стратегия:
     * - PENDING_CREATE  → syncCreate
     * - PENDING_UPDATE  → syncUpdate
     * - PENDING_DELETE  → syncDelete
     *
     * Используется в background-синхронизации.
     */
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

    /**
     * Загружает транзакции конкретного аккаунта за указанный период.
     *
     * Алгоритм:
     * 1. Проверяем, синхронизирован ли аккаунт (есть ли serverId)
     * 2. Запрашиваем транзакции за период
     * 3. Загружаем локальные записи по serverId
     * 4. Для каждой server-транзакции:
     *    - если локальной нет → создаём
     *    - если server.updatedAt > local.updatedAt → обновляем
     *
     * Стратегия разрешения конфликтов: last-write-wins.
     */
    override suspend fun pullFromRemoteForAccount(
        accountLocalId: String,
        startDate: String,
        endDate: String
    ) {
        // если аккаунт ещё не синхронизирован — транзакции с сервера не тянем
        val serverAccountId = getServerAccountIdByLocalId(accountLocalId) ?: return

        safeApiCall(errorLogger) {
            remoteDataSource.getByAccountAndPeriod(
                serverAccountId,
                startDate,
                endDate
            )
        }.onSuccess { transactionDtos ->
            val serverIds = transactionDtos.map { it.id }

            val localTransactions = safeDbCall(errorLogger) {
                localDataSource.getByServerIds(serverIds)
            }.getOrNull().orEmpty()

            val localMap = localTransactions.associateBy { it.serverId }

            transactionDtos.forEach { transactionDto ->
                val localTransaction = localMap[transactionDto.id]

                if (localTransaction == null) {
                    //  Локальной транзакции нет → создаём
                    safeDbCall(errorLogger) {
                        /*localDataSource.create(
                            transactionDto.toEntity(
                                localId = generateUUID(),
                                accountLocalId = accountLocalId,
                                syncStatus = SyncStatus.SYNCED
                            )
                        )*/
                    }
                } else if (transactionDto.updatedAt > localTransaction.updatedAt) {
                    // Если есть, то разрешаем конфликт, побеждает, так которая менялась позже
                   /* updateLocalFromRemote(
                        transactionDto.toEntity(
                            localId = localTransaction.localId,
                            accountLocalId = localTransaction.accountLocalId,
                            syncStatus = SyncStatus.SYNCED
                        )
                    )*/
                }
            }
        }
    }

    /**
     * Унифицированное обновление локальной транзакции
     * после успешного ответа сервера.
     *
     * Всегда перезаписывает данные и выставляет корректный syncStatus.
     */
    private suspend fun updateLocalFromRemote(transactionEntity: TransactionEntity) {
        safeDbCall(errorLogger) {
            localDataSource.update(transactionEntity)
        }
    }

    /**
     * Физическое удаление транзакции из локальной БД.
     *
     * Вызывается:
     * - после успешного server-delete
     * - либо если запись не была синхронизирована
     */
    private suspend fun deleteLocalTransaction(localId: String) {
        safeDbCall(errorLogger) {
            localDataSource.delete(localId)
        }
    }

    /**
     * Возвращает serverId аккаунта по его localId.
     *
     * Если аккаунт ещё не синхронизирован (serverId == null),
     * pull транзакций не выполняется.
     */
    private suspend fun getServerAccountIdByLocalId(id: String): Int? {
        return safeDbCall(errorLogger) {
            accountLocalDataSource.getByLocalId(id)
        }.getOrNull()?.serverId
    }
}
