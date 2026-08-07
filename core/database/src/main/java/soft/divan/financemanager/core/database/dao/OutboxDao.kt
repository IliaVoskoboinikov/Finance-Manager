package soft.divan.financemanager.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import soft.divan.financemanager.core.database.entity.OutboxEntryEntity

/**
 * Доступ к очереди исходящих операций ([OutboxEntryEntity]).
 *
 * Все изменения статуса адресуются по `sequenceNo` — первичному ключу записи очереди, а не по id
 * доменной сущности: над одной сущностью может висеть несколько операций подряд.
 */
@Dao
interface OutboxDao {

    /**
     * Ставит операцию в очередь и возвращает присвоенный `sequenceNo`.
     *
     * Вызывается **внутри** Room-транзакции доменного изменения — так запись данных и намерение
     * их отправить либо фиксируются вместе, либо вместе откатываются.
     */
    @Insert
    suspend fun insert(entry: OutboxEntryEntity): Long

    /**
     * Записи, готовые к отправке прямо сейчас: ждут своей очереди и уже отбыли backoff.
     *
     * Порядок по `sequenceNo` гарантирует, что счёт уедет раньше своих транзакций, а правки одной
     * сущности не перемешаются. [limit] ограничивает размер пачки, чтобы один прогон не занимал
     * сеть надолго.
     */
    @Query(
        "SELECT * FROM outbox " +
            "WHERE status = 'PENDING' AND nextAttemptAt <= :now " +
            "ORDER BY sequenceNo ASC LIMIT :limit"
    )
    suspend fun getReadyToSend(now: Long, limit: Int): List<OutboxEntryEntity>

    /** Помечает запись взятой в работу — чтобы параллельный прогон не отправил её повторно. */
    @Query(
        "UPDATE outbox SET status = 'IN_PROGRESS', updatedAt = :updatedAt " +
            "WHERE sequenceNo = :sequenceNo AND status = 'PENDING'"
    )
    suspend fun markInProgress(sequenceNo: Long, updatedAt: Long): Int

    /** Операция принята сервером — запись остаётся для наблюдаемости до очистки. */
    @Query(
        "UPDATE outbox SET status = 'COMPLETED', lastError = NULL, updatedAt = :updatedAt " +
            "WHERE sequenceNo = :sequenceNo"
    )
    suspend fun markCompleted(sequenceNo: Long, updatedAt: Long)

    /**
     * Возвращает запись в очередь после transient-ошибки: фиксирует попытку и момент, раньше
     * которого повторять бессмысленно.
     */
    @Query(
        "UPDATE outbox SET status = 'PENDING', attemptCount = :attemptCount, " +
            "nextAttemptAt = :nextAttemptAt, lastError = :lastError, updatedAt = :updatedAt " +
            "WHERE sequenceNo = :sequenceNo"
    )
    suspend fun scheduleRetry(
        sequenceNo: Long,
        attemptCount: Int,
        nextAttemptAt: Long,
        lastError: String?,
        updatedAt: Long
    )

    /**
     * Переводит запись в dead-letter: ретраи прекращены.
     *
     * Сюда попадают ошибки, которые повтор не исправит (например, отклонение по валидации), и
     * записи, исчерпавшие лимит попыток.
     */
    @Query(
        "UPDATE outbox SET status = 'FAILED', attemptCount = :attemptCount, " +
            "lastError = :lastError, updatedAt = :updatedAt WHERE sequenceNo = :sequenceNo"
    )
    suspend fun markFailed(
        sequenceNo: Long,
        attemptCount: Int,
        lastError: String?,
        updatedAt: Long
    )

    /** Записи в dead-letter — для индикации в UI и ручного повтора. */
    @Query("SELECT * FROM outbox WHERE status = 'FAILED' ORDER BY sequenceNo ASC")
    fun observeFailed(): Flow<List<OutboxEntryEntity>>

    /** Чистит успешно отправленные записи, чтобы очередь не росла бесконечно. */
    @Query("DELETE FROM outbox WHERE status = 'COMPLETED'")
    suspend fun deleteCompleted()

    @Query("DELETE FROM outbox")
    suspend fun deleteAll()
}
