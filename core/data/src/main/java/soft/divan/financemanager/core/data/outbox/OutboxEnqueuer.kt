package soft.divan.financemanager.core.data.outbox

import com.google.gson.Gson
import soft.divan.financemanager.core.data.source.OutboxLocalDataSource
import soft.divan.financemanager.core.data.util.coroutne.AppCoroutineContext
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
     * @param entityLocalId клиентский `localId`; он же используется как ключ идемпотентности,
     *   стабильный на все попытки отправки.
     * @param operation что делаем на сервере.
     * @param targetServerId адрес ресурса для `PUT`/`DELETE`; для `CREATE` не нужен.
     * @param body DTO тела запроса; `null` для операций без тела.
     */
    suspend fun enqueue(
        entityType: OutboxEntityType,
        entityLocalId: String,
        operation: OutboxOperation,
        targetServerId: String? = null,
        body: Any? = null
    ): Long {
        val now = clock.millis()

        // Разбор очереди планируется здесь, а не в вызывающем коде: так «положили операцию» и
        // «попробовали отправить» нельзя рассинхронизировать, забыв про второе. Внутри транзакции
        // launchSync откладывает запуск до commit — раньше отправлять нечего.
        appCoroutineContext.launchSync { processor.get().process() }

        return localDataSource.enqueue(
            OutboxEntryEntity(
                entityType = entityType,
                entityLocalId = entityLocalId,
                operation = operation,
                targetServerId = targetServerId,
                payload = body?.let { gson.toJson(it) } ?: EMPTY_PAYLOAD,
                idempotencyKey = entityLocalId,
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
