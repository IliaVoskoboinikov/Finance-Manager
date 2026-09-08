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
     * ### Барьер порядка
     * Одного `ORDER BY sequenceNo` для порядка **недостаточно**: запись, ушедшая в backoff или
     * удерживающая аренду, из выборки выпадает — и следующая операция той же сущности обгоняет
     * её. Так правка уезжала раньше создания и умирала с `404`, а удаление раньше создания
     * оставляло на сервере фантом.
     *
     * Поэтому подзапрос `NOT EXISTS` отсекает запись, перед которой в её группе
     * (`dependencyKey`) стоит любая **незакрытая** операция — независимо от того, попала она в
     * выборку или нет. Записи из разных групп друг друга не ждут.
     *
     * `FAILED` в барьер не входит намеренно: она уже никогда не выполнится, и держать за ней
     * группу вечно нельзя. Зависимые от неё записи уводит в dead-letter [failDependents].
     *
     * [limit] ограничивает размер пачки, чтобы один прогон не занимал сеть надолго.
     */
    @Query(
        "SELECT * FROM outbox AS o " +
            "WHERE ((o.status = 'PENDING' AND o.nextAttemptAt <= :now) " +
            "OR (o.status = 'IN_PROGRESS' AND o.updatedAt <= :staleBefore)) " +
            "AND NOT EXISTS (" +
            "SELECT 1 FROM outbox AS blocker " +
            "WHERE blocker.dependencyKey = o.dependencyKey " +
            "AND blocker.sequenceNo < o.sequenceNo " +
            "AND blocker.status IN ('PENDING', 'IN_PROGRESS')" +
            ") " +
            "ORDER BY o.sequenceNo ASC LIMIT :limit"
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
     *
     * Вместе с самой записью в dead-letter уходят **зависящие от неё** — незакрытые операции той
     * же группы, стоящие после неё. Отправлять их бессмысленно: правка по несуществующему ресурсу
     * вернёт `404`, транзакция без своего счёта — `400`. Каждая честно потратила бы все восемь
     * попыток и всё равно оказалась бы здесь — только позже и с невнятной причиной. Ручной повтор
     * возвращает всю группу целиком, поэтому исправить ситуацию можно одним действием.
     *
     * Обе части — один запрос, а не два: иначе между ними появилось бы окно, в котором зависимая
     * операция уже готова к отправке, а её предшественник ещё не помечен провалившимся.
     *
     * [attemptCount] и [lastError] относятся к самой записи; зависимым проставляется общий текст,
     * а их счётчик попыток остаётся нетронутым — они не виноваты.
     *
     * Результат — сколько записей затронуто (сама плюс зависимые).
     */
    @Query(
        "UPDATE outbox SET status = 'FAILED', " +
            "attemptCount = CASE WHEN sequenceNo = :sequenceNo THEN :attemptCount " +
            "ELSE attemptCount END, " +
            "lastError = CASE WHEN sequenceNo = :sequenceNo THEN :lastError " +
            "ELSE 'Отменена: предыдущая операция группы не выполнена' END, " +
            "updatedAt = :updatedAt " +
            "WHERE sequenceNo = :sequenceNo " +
            "OR (dependencyKey = :dependencyKey AND sequenceNo > :sequenceNo " +
            "AND status IN ('PENDING', 'IN_PROGRESS'))"
    )
    suspend fun markFailed(
        sequenceNo: Long,
        dependencyKey: String,
        attemptCount: Int,
        lastError: String?,
        updatedAt: Long
    ): Int

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
