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
     * Записи, готовые к отправке прямо сейчас.
     *
     * Это либо ждущие своей очереди и отбывшие backoff (`PENDING`), либо **зависшие в работе**:
     * взятые прошлым прогоном, который не успел доложить об исходе. Такое случается, когда система
     * убивает процесс во время сетевого вызова, — без второго условия эти записи не подобрал бы
     * никто и они молча пропали бы навсегда.
     *
     * `updatedAt <= staleBefore` работает как «аренда»: пока она не истекла, запись считается
     * живой и чужому прогону недоступна.
     *
     * Порядок по `sequenceNo` гарантирует, что счёт уедет раньше своих транзакций, а правки одной
     * сущности не перемешаются. [limit] ограничивает размер пачки, чтобы один прогон не занимал
     * сеть надолго.
     */
    @Query(
        "SELECT * FROM outbox " +
            "WHERE (status = 'PENDING' AND nextAttemptAt <= :now) " +
            "OR (status = 'IN_PROGRESS' AND updatedAt <= :staleBefore) " +
            "ORDER BY sequenceNo ASC LIMIT :limit"
    )
    suspend fun getReadyToSend(now: Long, staleBefore: Long, limit: Int): List<OutboxEntryEntity>

    /**
     * Берёт запись в работу и продлевает аренду; `0` означает, что взять не удалось.
     *
     * Взять можно либо свободную (`PENDING`), либо ту, чья аренда истекла. Живую `IN_PROGRESS`
     * увести нельзя — это и защищает от двойной отправки, когда прогоны пересеклись.
     */
    @Query(
        "UPDATE outbox SET status = 'IN_PROGRESS', updatedAt = :updatedAt " +
            "WHERE sequenceNo = :sequenceNo " +
            "AND (status = 'PENDING' OR (status = 'IN_PROGRESS' AND updatedAt <= :staleBefore))"
    )
    suspend fun markInProgress(sequenceNo: Long, staleBefore: Long, updatedAt: Long): Int

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

    /** Сколько операций осело в dead-letter — источник индикатора «не отправлено» в UI. */
    @Query("SELECT COUNT(*) FROM outbox WHERE status = 'FAILED'")
    fun observeFailedCount(): Flow<Int>

    /**
     * Возвращает записи из dead-letter в очередь по явной команде пользователя.
     *
     * Счётчик попыток и время следующей попытки сбрасываются: ручной повтор — это утверждение
     * «причина устранена», поэтому история прошлых неудач не должна мешать новой отправке.
     */
    @Query(
        "UPDATE outbox SET status = 'PENDING', attemptCount = 0, nextAttemptAt = 0, " +
            "lastError = NULL, updatedAt = :updatedAt WHERE status = 'FAILED'"
    )
    suspend fun requeueFailed(updatedAt: Long): Int

    /** Чистит успешно отправленные записи, чтобы очередь не росла бесконечно. */
    @Query("DELETE FROM outbox WHERE status = 'COMPLETED'")
    suspend fun deleteCompleted()

    @Query("DELETE FROM outbox")
    suspend fun deleteAll()
}
