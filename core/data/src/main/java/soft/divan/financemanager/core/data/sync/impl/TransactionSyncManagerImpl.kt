package soft.divan.financemanager.core.data.sync.impl

import kotlinx.coroutines.flow.first
import soft.divan.financemanager.core.data.dto.TransactionDto
import soft.divan.financemanager.core.data.mapper.ApiDateMapper
import soft.divan.financemanager.core.data.mapper.TimeMapper
import soft.divan.financemanager.core.data.mapper.toEntity
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.financemanager.core.data.source.CategoryLocalDataSource
import soft.divan.financemanager.core.data.source.TransactionLocalDataSource
import soft.divan.financemanager.core.data.source.TransactionRemoteDataSource
import soft.divan.financemanager.core.data.sync.Synchronizer
import soft.divan.financemanager.core.data.sync.TransactionSyncManager
import soft.divan.financemanager.core.data.sync.isPullSkipped
import soft.divan.financemanager.core.data.util.generateUUID
import soft.divan.financemanager.core.data.util.safeCall.safeApiCall
import soft.divan.financemanager.core.data.util.safeCall.safeDbCall
import soft.divan.financemanager.core.database.entity.AccountEntity
import soft.divan.financemanager.core.database.entity.TransactionEntity
import soft.divan.financemanager.core.database.model.SyncStatus
import soft.divan.financemanager.core.domain.model.TransactionType
import soft.divan.financemanager.core.domain.result.fold
import soft.divan.financemanager.core.domain.result.getOrNull
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
     * Возвращает результат pull'а: `safeApiCall` исключений не бросает, поэтому судить об
     * успехе можно только по нему, а не по отсутствию исключения.
     */
    override suspend fun syncWith(synchronizer: Synchronizer): Boolean = pullServerData()

    /**
     * Выполняет pull-синхронизацию для всех локальных аккаунтов.
     *
     * Для каждого аккаунта:
     * - определяется период (от createdAt - 2 дня до текущего момента)
     * - запрашиваются транзакции с сервера
     *
     * Почему -2 дня:
     * Защита от пограничных кейсов по времени и рассинхронизации часов.
     *
     * Сбой одного счёта не останавливает остальные: результат — успех всех счетов.
     */
    override suspend fun pullServerData(): Boolean =
        accountLocalDataSource.getAll().first()
            .map { account ->
                pullFromRemoteForAccount(
                    accountLocalId = account.localId,
                    startDate = ApiDateMapper.toApiDate(
                        Instant.parse(account.createdAt).minus(2, ChronoUnit.DAYS)
                    ),
                    endDate = ApiDateMapper.toApiDate(Instant.now())
                )
            }
            // map, а не all: all остановился бы на первом сбое и не дотянул бы остальные счета
            .all { it }

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
     *
     * Счёт без `serverId` — не сбой: тянуть нечего, его создание ещё в очереди.
     */
    @Suppress("ReturnCount")
    override suspend fun pullFromRemoteForAccount(
        accountLocalId: String,
        startDate: String,
        endDate: String
    ): Boolean {
        val account = safeDbCall(errorLogger) {
            accountLocalDataSource.getByLocalId(accountLocalId)
        }.getOrNull() ?: return false

        val serverAccountId = account.serverId ?: return true

        return safeApiCall(errorLogger) {
            remoteDataSource.getByAccountAndPeriod(
                serverAccountId,
                startDate,
                endDate
            )
        }.fold(
            onSuccess = { transactionDtos -> mergeServerTransactions(account, transactionDtos) },
            onFailure = { error -> error.isPullSkipped() }
        )
    }

    /**
     * Сливает серверные транзакции счёта с локальными; `false` — локальные не удалось прочитать.
     *
     * Без прочитанных локальных записей слияние прерывается: иначе каждая серверная транзакция
     * выглядела бы новой и вставлялась бы дубликатом с новым `localId` — на каждом синке.
     */
    private suspend fun mergeServerTransactions(
        account: AccountEntity,
        transactionDtos: List<TransactionDto>
    ): Boolean {
        val serverIds = transactionDtos.map { it.id }

        val localTransactions = safeDbCall(errorLogger) {
            localDataSource.getBySyncIds(serverIds)
        }.getOrNull() ?: return false

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
                accountLocalId = account.localId,
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
        return true
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
