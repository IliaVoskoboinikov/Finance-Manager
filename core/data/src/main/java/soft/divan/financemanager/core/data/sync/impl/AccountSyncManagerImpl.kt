package soft.divan.financemanager.core.data.sync.impl

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import soft.divan.financemanager.core.data.dto.AccountDto
import soft.divan.financemanager.core.data.dto.CreateAccountRequestDto
import soft.divan.financemanager.core.data.mapper.toDto
import soft.divan.financemanager.core.data.mapper.toEntity
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.financemanager.core.data.source.AccountRemoteDataSource
import soft.divan.financemanager.core.data.sync.AccountSyncManager
import soft.divan.financemanager.core.data.sync.util.Synchronizer
import soft.divan.financemanager.core.data.util.generateUUID
import soft.divan.financemanager.core.data.util.safeCall.safeApiCall
import soft.divan.financemanager.core.data.util.safeCall.safeDbCall
import soft.divan.financemanager.core.database.entity.AccountEntity
import soft.divan.financemanager.core.database.model.SyncStatus
import soft.divan.financemanager.core.domain.result.getOrNull
import soft.divan.financemanager.core.domain.result.onSuccess
import soft.divan.financemanager.core.loggingerror.ErrorLogger
import javax.inject.Inject

/**
 * Реализация [AccountSyncManager].
 *
 * Отвечает за двустороннюю синхронизацию аккаунтов:
 *
 * 1. Pull — получение данных с сервера (server → local)
 * 2. Push — отправка локальных изменений (local → server)
 *
 * Особенности реализации:
 * - Offline-first: локальная БД является источником истины
 * - Разрешение конфликтов по updatedAt (last-write-wins)
 * - Double-check locking через Mutex для защиты от параллельного pull
 * - Все операции обёрнуты в safeApiCall / safeDbCall
 *
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
     * Порядок выполнения:
     * 1. pullServerData()  — обновление локальной БД
     * 2. pushLocalChanges() — отправка pending-изменений
     *
     * Возвращает true, если оба шага завершились без исключений.
     */
    override suspend fun syncWith(synchronizer: Synchronizer): Boolean {
        return runCatching {
            pullServerData()
            pushLocalChanges()
        }.isSuccess
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
                localDataSource.getByServerIds(serverIds)
            }.getOrNull().orEmpty()

            val localMap = localAccounts.associateBy { it.serverId }

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
                } else if (accountDto.updatedAt > localAccount.updatedAt) {
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
     * Отправляет новый локальный аккаунт на сервер.
     *
     * После успешного ответа:
     * - обновляет локальную запись
     * - устанавливает serverId
     * - помечает как SYNCED
     */
    override suspend fun syncCreate(accountDto: CreateAccountRequestDto, localId: String) {
        safeApiCall(errorLogger) {
            remoteDataSource.create(accountDto)
        }.onSuccess { accountDto ->
            updateLocalFromRemote(accountDto = accountDto, localId = localId)
        }
    }

    /**
     * Отправляет обновление аккаунта на сервер.
     *
     * Требует наличия serverId.
     * После успешного ответа синхронизирует локальную запись.
     */
    override suspend fun syncUpdate(accountEntity: AccountEntity) {
        accountEntity.serverId?.let { idAccount ->
            safeApiCall(errorLogger) {
                remoteDataSource.update(
                    id = idAccount,
                    account = accountEntity.toDto()
                )
            }.onSuccess { accountDto ->
                updateLocalFromRemote(accountDto = accountDto, localId = accountEntity.localId)
            }
        }
    }

    /**
     * Удаляет аккаунт.
     *
     * Если serverId == null:
     * - запись никогда не была синхронизирована → удаляем локально
     *
     * Если serverId != null:
     * - удаляем на сервере
     * - затем удаляем локально
     */
    override suspend fun syncDelete(accountEntity: AccountEntity) {
        if (accountEntity.serverId == null) {
            deleteLocalAccount(accountEntity.localId)
        } else {
            safeApiCall(errorLogger) {
                remoteDataSource.delete(accountEntity.serverId!!)
            }.onSuccess {
                deleteLocalAccount(accountEntity.localId)
            }
        }
    }

    /**
     * Обрабатывает все локальные записи со статусом pending.
     *
     * Для каждой записи:
     * - PENDING_CREATE  → syncCreate
     * - PENDING_UPDATE  → syncUpdate
     * - PENDING_DELETE  → syncDelete
     *
     * Используется при background-синхронизации.
     */
    private suspend fun pushLocalChanges() {
        safeDbCall(errorLogger) { localDataSource.getPendingSync() }.onSuccess { accountEntities ->
            accountEntities.forEach { accountEntity ->
                when (accountEntity.syncStatus) {
                    SyncStatus.SYNCED -> Unit

                    SyncStatus.PENDING_CREATE -> syncCreate(
                        accountDto = accountEntity.toDto(),
                        localId = accountEntity.localId
                    )

                    SyncStatus.PENDING_UPDATE -> syncUpdate(accountEntity)

                    SyncStatus.PENDING_DELETE -> syncDelete(accountEntity)
                }
            }
        }
    }

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

    /**
     * Физическое удаление записи из локальной БД.
     *
     * Используется только после успешного server-delete
     * или если запись не была синхронизирована.
     */
    private suspend fun deleteLocalAccount(localId: String) {
        safeDbCall(errorLogger) {
            localDataSource.delete(localId)
        }
    }
}
