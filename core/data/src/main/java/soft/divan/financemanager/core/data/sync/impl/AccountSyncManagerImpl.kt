package soft.divan.financemanager.core.data.sync.impl

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import soft.divan.financemanager.core.data.dto.AccountDto
import soft.divan.financemanager.core.data.dto.CreateAccountRequestDto
import soft.divan.financemanager.core.data.mapper.TimeMapper
import soft.divan.financemanager.core.data.mapper.toDto
import soft.divan.financemanager.core.data.mapper.toEntity
import soft.divan.financemanager.core.data.mapper.toUpdateDto
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.financemanager.core.data.source.AccountRemoteDataSource
import soft.divan.financemanager.core.data.sync.AccountSyncManager
import soft.divan.financemanager.core.data.sync.util.Synchronizer
import soft.divan.financemanager.core.data.util.generateUUID
import soft.divan.financemanager.core.data.util.safeCall.safeApiCall
import soft.divan.financemanager.core.data.util.safeCall.safeDbCall
import soft.divan.financemanager.core.database.entity.AccountEntity
import soft.divan.financemanager.core.database.model.SyncStatus
import soft.divan.financemanager.core.domain.model.AccountStatus
import soft.divan.financemanager.core.domain.result.DomainResult
import soft.divan.financemanager.core.domain.result.fold
import soft.divan.financemanager.core.domain.result.getOrNull
import soft.divan.financemanager.core.domain.result.onSuccess
import soft.divan.financemanager.core.loggingerror.ErrorLogger
import javax.inject.Inject

/**
 * Реализация [AccountSyncManager].
 *
 * Отвечает за одно направление — **pull**, получение данных с сервера (server → local).
 * Обратное направление обеспечивает очередь исходящих операций (см. `docs/outbox.md`).
 *
 * Особенности реализации:
 * - Offline-first: локальная БД является источником истины
 * - Разрешение конфликтов по updatedAt (last-write-wins), но строку с неотправленной операцией
 *   серверная версия не перезаписывает — см. [canBeOverwritten]
 * - Double-check locking через Mutex для защиты от параллельного pull
 * - Все операции обёрнуты в safeApiCall / safeDbCall
 */
class AccountSyncManagerImpl @Inject constructor(
    private val remoteDataSource: AccountRemoteDataSource,
    private val localDataSource: AccountLocalDataSource,
    private val errorLogger: ErrorLogger
) : AccountSyncManager {

    /**
     * Mutex защищает pullServerData() от параллельного выполнения.
     * Нужен, так как sync может запускаться:
     * - из WorkManager
     * - из Repository
     * - вручную
     */
    private val pullMutex = Mutex()

    /**
     * Флаг для double-check locking.
     *
     * @Volatile гарантирует корректную публикацию состояния между потоками.
     */
    @Volatile
    private var isPulling = false

    /**
     * Точка входа для полной синхронизации.
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
     * Загружает актуальные данные с сервера и обновляет локальную БД.
     *
     * Использует double-check locking:
     * 1. Проверка флага без блокировки
     * 2. Повторная проверка внутри Mutex
     *
     * Это предотвращает параллельный pull и дублирование записей.
     */
    override suspend fun pullServerData() {
        // First check (без блокировки)
        if (isPulling) return

        pullMutex.withLock {
            // Second check (уже под блокировкой)
            if (isPulling) return
            isPulling = true
            try {
                performPull()
            } finally {
                isPulling = false
            }
        }
    }

    /**
     * Основная логика pull-синхронизации.
     *
     * Алгоритм:
     * 1. Получаем список аккаунтов с сервера
     * 2. Загружаем соответствующие локальные записи по serverId
     * 3. Для каждого server-аккаунта:
     *    - если локального нет → создаём
     *    - если есть и server.updatedAt > local.updatedAt → обновляем
     *
     * Стратегия разрешения конфликтов: last-write-wins.
     */
    private suspend fun performPull() {
        safeApiCall(errorLogger) {
            remoteDataSource.getAll()
        }.onSuccess { accountDtos ->
            val serverIds = accountDtos.map { it.id }

            val localAccounts = safeDbCall(errorLogger) {
                localDataSource.getBySyncIds(serverIds)
            }.getOrNull().orEmpty()

            // Ключ — serverId, а для созданных здесь и ещё не подтверждённых записей (create ушёл
            // с `id = localId`, но ACK не дошёл) — их localId: сервер знает их именно под ним.
            // Без этого pull принял бы собственный счёт за новый и вставил дубликат.
            val localMap = localAccounts.associateBy { it.serverId ?: it.localId }

            accountDtos.forEach { accountDto ->
                val localAccount = localMap[accountDto.id]

                if (localAccount == null) {
                    // Локального аккаунта нет → создаём
                    safeDbCall(errorLogger) {
                        localDataSource.create(
                            accountDto.toEntity(
                                localId = generateUUID(),
                                syncStatus = SyncStatus.SYNCED
                            )
                        )
                    }
                } else if (localAccount.canBeOverwritten(accountDto.updatedAt)) {
                    // Конфликт → побеждает тот, кто обновлялся позже
                    updateLocalFromRemote(
                        accountDto = accountDto,
                        localId = localAccount.localId
                    )
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
     * воскресила бы удалённый счёт в списках. Данные при этом не терялись бы — очередь хранит
     * снимок и всё равно доотправит его, — но состояние на экране успело бы соврать.
     *
     * Дождаться отправки безопасно: после неё запись станет `SYNCED`, и ближайший pull разрешит
     * конфликт уже честно.
     */
    private fun AccountEntity.canBeOverwritten(serverUpdatedAt: String): Boolean =
        syncStatus == SyncStatus.SYNCED && TimeMapper.isAfter(serverUpdatedAt, updatedAt)

    /**
     * Унифицированный метод обновления локальной записи
     * после успешного ответа сервера.
     *
     * Всегда:
     * - перезаписывает данные
     * - выставляет syncStatus = SYNCED
     */
    private suspend fun updateLocalFromRemote(accountDto: AccountDto, localId: String) {
        safeDbCall(errorLogger) {
            localDataSource.update(
                accountDto.toEntity(
                    localId = localId,
                    syncStatus = SyncStatus.SYNCED
                )
            )
        }
    }
}
