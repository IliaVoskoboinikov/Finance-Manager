package soft.divan.financemanager.core.data.outbox

import com.google.gson.Gson
import soft.divan.financemanager.core.data.source.OutboxLocalDataSource
import soft.divan.financemanager.core.data.util.coroutine.AppCoroutineContext
import soft.divan.financemanager.core.data.util.generateUUID
import soft.divan.financemanager.core.database.entity.OutboxEntryEntity
import soft.divan.financemanager.core.database.model.OutboxEntityType
import soft.divan.financemanager.core.database.model.OutboxOperation
import soft.divan.financemanager.core.database.model.OutboxStatus
import java.time.Clock
import javax.inject.Inject
import javax.inject.Provider

/** Тело для операций, у которых его нет (`DELETE`). */
private const val EMPTY_PAYLOAD = "{}"

/**
 * Ставит исходящую операцию в очередь (Transactional Outbox).
 *
 * **Вызывать только внутри `TransactionRunner.runInTransaction`, рядом с доменной записью.**
 * Тогда «данные изменены» и «операцию надо отправить» фиксируются одной Room-транзакцией: при
 * откате исчезает и то, и другое, и на сервер не уйдёт операция, которой нет локально. Это и есть
 * смысл паттерна — убрать ненадёжную сеть из критического пути записи.
 *
 * Тело запроса сериализуется **здесь и сейчас**: очередь хранит снимок операции, а не ссылку на
 * текущее состояние строки, поэтому последующие правки сущности не изменят уже поставленную
 * операцию.
 *
 * Отправкой занимается `OutboxProcessor`; доставка — «хотя бы раз», единственность эффекта
 * обеспечивает идемпотентность (см. [docs/idempotency.md](../../../../../../../../../../docs/idempotency.md)).
 */
class OutboxEnqueuer @Inject constructor(
    private val localDataSource: OutboxLocalDataSource,
    private val gson: Gson,
    private val clock: Clock,
    private val appCoroutineContext: AppCoroutineContext,
    private val processor: Provider<OutboxProcessor>
) {

    /**
     * Записывает операцию в очередь и возвращает её `sequenceNo` (он же порядок отправки).
     *
     * @param entityType тип доменной сущности — определяет эндпоинт.
     * @param entityLocalId клиентский `localId` — адрес доменной строки.
     * @param dependencyKey `localId` сущности, которая обязана уехать на сервер **раньше** этой
     *   операции. Счёт передаёт свой `localId` (он ни от кого не зависит), транзакция — `localId`
     *   своего счёта. Это ссылка на предшественника, а не номер группы: транзакции одного счёта
     *   ждут счёт, но не друг друга.
     * @param operation что делаем на сервере.
     * @param targetServerId адрес ресурса для `PUT`/`DELETE`; для `CREATE` не нужен.
     * @param body DTO тела запроса; `null` для операций без тела.
     */
    @Suppress("LongParameterList")
    suspend fun enqueue(
        entityType: OutboxEntityType,
        entityLocalId: String,
        dependencyKey: String,
        operation: OutboxOperation,
        targetServerId: String? = null,
        body: Any? = null
    ): Long {
        val now = clock.millis()

        // Разбор очереди планируется здесь, а не в вызывающем коде: так «положили операцию» и
        // «попробовали отправить» нельзя рассинхронизировать, забыв про второе. Внутри транзакции
        // launchSync откладывает запуск до commit — раньше отправлять нечего.
        appCoroutineContext.launchSync {
            processor.get().process()
        }

        return localDataSource.enqueue(
            OutboxEntryEntity(
                entityType = entityType,
                entityLocalId = entityLocalId,
                dependencyKey = dependencyKey,
                operation = operation,
                targetServerId = targetServerId,
                payload = body?.let { gson.toJson(it) } ?: EMPTY_PAYLOAD,
                // Ключ принадлежит ОПЕРАЦИИ, а не сущности: у создания и последующей правки одной
                // строки ключи разные, иначе сервер счёл бы правку повтором создания. Генерируется
                // один раз здесь и переживает все повторы вместе с записью очереди.
                idempotencyKey = generateUUID(),
                status = OutboxStatus.PENDING,
                attemptCount = 0,
                // Ноль — «можно отправлять немедленно»; backoff проставляется только при повторах.
                nextAttemptAt = 0,
                lastError = null,
                createdAt = now,
                updatedAt = now
            )
        )
    }
}
