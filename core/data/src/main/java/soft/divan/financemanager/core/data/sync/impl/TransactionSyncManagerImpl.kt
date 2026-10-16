package soft.divan.financemanager.core.data.sync.impl

import kotlinx.coroutines.flow.first
import soft.divan.financemanager.core.data.mapper.ApiDateMapper
import soft.divan.financemanager.core.data.mapper.TimeMapper
import soft.divan.financemanager.core.data.mapper.toEntity
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.financemanager.core.data.source.CategoryLocalDataSource
import soft.divan.financemanager.core.data.source.TransactionLocalDataSource
import soft.divan.financemanager.core.data.source.TransactionRemoteDataSource
import soft.divan.financemanager.core.data.sync.Synchronizer
import soft.divan.financemanager.core.data.sync.TransactionSyncManager
import soft.divan.financemanager.core.data.util.generateUUID
import soft.divan.financemanager.core.data.util.safeCall.safeApiCall
import soft.divan.financemanager.core.data.util.safeCall.safeDbCall
import soft.divan.financemanager.core.database.entity.TransactionEntity
import soft.divan.financemanager.core.database.model.SyncStatus
import soft.divan.financemanager.core.domain.model.TransactionType
import soft.divan.financemanager.core.domain.result.getOrNull
import soft.divan.financemanager.core.domain.result.onSuccess
import soft.divan.financemanager.core.loggingerror.ErrorLogger
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Реализация [TransactionSyncManager].
 *
 * Отвечает за одно направление — **pull**, загрузку транзакций с сервера по каждому аккаунту.
 * Обратное направление обеспечивает очередь исходящих операций (см. `docs/outbox.md`).
 *
 * Архитектурные принципы:
 * - Offline-first: локальная БД — источник истины
 * - Синхронизация выполняется на уровне аккаунта
 * - Разрешение конфликтов по updatedAt (last-write-wins), но строку с неотправленной операцией
 *   серверная версия не перезаписывает — см. [canBeOverwritten]
 * - Все операции изолированы через safeApiCall / safeDbCall
 */
class TransactionSyncManagerImpl @Inject constructor(
    private val remoteDataSource: TransactionRemoteDataSource,
    private val localDataSource: TransactionLocalDataSource,
    private val accountLocalDataSource: AccountLocalDataSource,
    private val categoryLocalDataSource: CategoryLocalDataSource,
    private val errorLogger: ErrorLogger
) : TransactionSyncManager {

    /**
     * Точка входа полной синхронизации.
     *
     * Тянет актуальные данные с сервера. Отправку локальных изменений выполняет очередь
     * исходящих операций, а не этот менеджер.
     *
     * Возвращает true, если шаг завершился без исключений.
     */
    override suspend fun syncWith(synchronizer: Synchronizer): Boolean {
        return runCatching { pullServerData() }.isSuccess
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
        val account = safeDbCall(errorLogger) {
            accountLocalDataSource.getByLocalId(accountLocalId)
        }.getOrNull() ?: return

        val serverAccountId = account.serverId ?: return

        safeApiCall(errorLogger) {
            remoteDataSource.getByAccountAndPeriod(
                serverAccountId,
                startDate,
                endDate
            )
        }.onSuccess { transactionDtos ->
            val serverIds = transactionDtos.map { it.id }

            val localTransactions = safeDbCall(errorLogger) {
                localDataSource.getBySyncIds(serverIds)
            }.getOrNull().orEmpty()

            // Ключ — serverId, а для созданных здесь и ещё не подтверждённых записей (create ушёл
            // с `id = localId`, но ACK не дошёл) — их localId: сервер знает их именно под ним.
            // Без этого pull принял бы собственную транзакцию за новую и вставил дубликат.
            val localMap = localTransactions.associateBy { it.serverId ?: it.localId }

            transactionDtos.forEach { transactionDto ->
                val localTransaction = localMap[transactionDto.id]

                val category =
                    categoryLocalDataSource.getById(transactionDto.categoryId) ?: return@forEach
                val type = if (category.isIncome) TransactionType.INCOME else TransactionType.EXPENSE

                val entity = transactionDto.toEntity(
                    localId = localTransaction?.localId ?: generateUUID(),
                    accountLocalId = accountLocalId,
                    currencyId = account.currencyId,
                    type = type,
                    syncStatus = SyncStatus.SYNCED
                )

                if (localTransaction == null) {
                    //  Локальной транзакции нет → создаём
                    safeDbCall(errorLogger) { localDataSource.insert(entity) }
                } else if (localTransaction.canBeOverwritten(transactionDto.updatedAt)) {
                    // Если есть, то разрешаем конфликт: побеждает та, что менялась позже
                    updateLocalFromRemote(entity)
                }
            }
        }
    }

    /**
     * Можно ли принять серверную версию поверх локальной.
     *
     * Два условия. Первое — обычный last-write-wins по времени изменения.
     *
     * Второе: строка не должна ждать отправки. Пока `syncStatus` не `SYNCED`, у записи есть
     * незавершённая операция в очереди, и серверная версия заведомо не знает о ней. Перезапись
     * в этот момент откатила бы правку на глазах у пользователя, а `PENDING_DELETE` и вовсе
     * вернула бы удалённую транзакцию в списки. Данные при этом не терялись бы — очередь хранит
     * снимок и всё равно доотправит его, — но состояние на экране успело бы соврать.
     *
     * Дождаться отправки безопасно: после неё запись станет `SYNCED`, и ближайший pull разрешит
     * конфликт уже честно.
     */
    private fun TransactionEntity.canBeOverwritten(serverUpdatedAt: String): Boolean =
        syncStatus == SyncStatus.SYNCED && TimeMapper.isAfter(serverUpdatedAt, updatedAt)

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
}
