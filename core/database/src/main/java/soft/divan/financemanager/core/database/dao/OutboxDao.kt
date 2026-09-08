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
     * Поэтому подзапрос `NOT EXISTS` отсекает запись, перед которой стоит незакрытая операция —
     * но **только настоящий предшественник**, а их ровно три вида:
     *
     * 1. `blocker.entityLocalId = o.entityLocalId` — прошлая операция **той же строки**: правку
     *    нельзя слать раньше создания, удаление раньше правки.
     * 2. `blocker.entityLocalId = o.dependencyKey` — операция **предшественника**: транзакцию
     *    нельзя слать раньше её счёта, иначе сервер отвергнет её с неизвестным `accountId`.
     * 3. `blocker.dependencyKey = o.entityLocalId` — операция **того, кто зависит от нас**:
     *    удаление счёта нельзя слать раньше ещё не уехавших транзакций этого счёта, иначе они
     *    приедут к удалённому счёту и получат `400`.
     *
     * Третье ребро — обратное второму, и без него связь односторонняя: «транзакция после счёта»
     * соблюдалось, а «удаление счёта после его транзакций» — нет.
     *
     * Важно, чего в подзапросе **нет**: сравнения `dependencyKey` между собой. Две транзакции
     * одного счёта — ровесники: обе ждут счёт, но не друг друга. Держать их по одной было бы
     * напрасной сериализацией — на 50 транзакциях счёта выборка отдавала бы по одной за проход.
     *
     * Первое условие намеренно не ограничено предшественником: если сущность сменит владельца,
     * её операции сошлются на разных предшественников, но порядок между ними обязан сохраниться.
     *
     * Цикл невозможен: блокировать может только запись с **меньшим** `sequenceNo`.
     *
     * `FAILED` в барьер не входит намеренно: она уже никогда не выполнится, и держать за ней
     * очередь вечно нельзя. Зависимые от неё записи уводит в dead-letter [markFailed].
     *
     * [limit] ограничивает размер пачки, чтобы один прогон не занимал сеть надолго.
     */
    @Query(
        "SELECT * FROM outbox AS o " +
            "WHERE ((o.status = 'PENDING' AND o.nextAttemptAt <= :now) " +
            "OR (o.status = 'IN_PROGRESS' AND o.updatedAt <= :staleBefore)) " +
            "AND NOT EXISTS (" +
            "SELECT 1 FROM outbox AS blocker " +
            "WHERE blocker.sequenceNo < o.sequenceNo " +
            "AND blocker.status IN ('PENDING', 'IN_PROGRESS') " +
            "AND (blocker.entityLocalId = o.entityLocalId " +
            "OR blocker.entityLocalId = o.dependencyKey " +
            "OR blocker.dependencyKey = o.entityLocalId)" +
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
     * Вместе с самой записью в dead-letter уходят **зависящие от неё**, и это ровно те же два
     * ребра, что проверяет барьер в [getReadyToSend]: последующие операции **той же строки** и
     * операции, назвавшие её своим предшественником (`dependencyKey`). Отправлять их
     * бессмысленно: правка по несуществующему ресурсу вернёт `404`, транзакция без своего
     * счёта — `400`. Каждая честно потратила бы все восемь попыток и всё равно оказалась бы
     * здесь — только позже и с невнятной причиной. Ручной повтор возвращает их вместе с
     * предшественником, поэтому чинится это одним действием.
     *
     * Ровесники не затрагиваются: если провалилась одна транзакция счёта, остальные транзакции
     * того же счёта от неё не зависят и продолжают отправляться.
     *
     * Обе части — один запрос, а не два: иначе между ними появилось бы окно, в котором зависимая
     * операция уже готова к отправке, а её предшественник ещё не помечен провалившимся.
     *
     * Каскад трогает только `PENDING`. Запись в `IN_PROGRESS` прямо сейчас отправляет другой
     * прогон, и он доложит о её исходе сам: пометив её `FAILED`, мы получили бы гонку — его
     * `scheduleRetry` вернул бы её в `PENDING` и отменил бы отмену.
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
            "ELSE 'Отменена: предыдущая операция не выполнена' END, " +
            "updatedAt = :updatedAt " +
            "WHERE sequenceNo = :sequenceNo " +
            "OR (sequenceNo > :sequenceNo AND status = 'PENDING' " +
            "AND (entityLocalId = :entityLocalId OR dependencyKey = :entityLocalId))"
    )
    suspend fun markFailed(
        sequenceNo: Long,
        entityLocalId: String,
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
