package soft.divan.financemanager.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import soft.divan.financemanager.core.database.model.OutboxEntityType
import soft.divan.financemanager.core.database.model.OutboxOperation
import soft.divan.financemanager.core.database.model.OutboxStatus

/**
 * Запись очереди исходящих операций (Transactional Outbox).
 *
 * Строка создаётся **в той же Room-транзакции**, что и доменное изменение, поэтому «данные
 * записаны» и «операцию надо отправить» становятся атомарной парой: при откате транзакции
 * исчезает и то, и другое, а значит на сервер не уйдёт ничего, чего нет локально.
 *
 * Отправкой занимается `OutboxProcessor`: он забирает готовые записи в порядке [sequenceNo],
 * шлёт [payload] и переводит запись в [OutboxStatus.COMPLETED] либо планирует повтор.
 *
 * Доставка гарантируется «хотя бы раз» (at-least-once) — повтор возможен, если ответ сервера не
 * дошёл. Единственность эффекта обеспечивает идемпотентность на стороне сервера
 * (см. [docs/idempotency.md](../../../../../../../../../../docs/idempotency.md)).
 *
 * @property sequenceNo Порядковый номер, он же первичный ключ. Задаёт FIFO-порядок отправки:
 *   счёт уходит раньше своих транзакций, а правки одной сущности — в порядке их появления.
 * @property entityType Тип доменной сущности — определяет эндпоинт отправки.
 * @property entityLocalId Клиентский `localId` сущности. Связывает запись очереди с доменной
 *   строкой (для отображения статуса и ручного повтора).
 * @property operation Что именно делаем на сервере.
 * @property payload Снимок тела запроса (JSON) на момент операции. Хранится готовым, чтобы
 *   отправка не зависела от последующих правок доменной строки: очередь — это журнал событий,
 *   а не указатель на текущее состояние.
 * @property idempotencyKey Ключ дедупликации, стабильный на все попытки. Для операций, адресуемых
 *   идентификатором ресурса, совпадает с [entityLocalId].
 * @property status Текущее состояние записи в очереди.
 * @property attemptCount Число уже выполненных попыток отправки — основа экспоненциального
 *   backoff и критерий ухода в dead-letter.
 * @property nextAttemptAt Момент (epoch millis), раньше которого запись не берётся в работу.
 * @property lastError Текст последней ошибки — для диагностики записей в [OutboxStatus.FAILED].
 * @property createdAt Момент постановки в очередь (epoch millis).
 * @property updatedAt Момент последнего изменения записи (epoch millis).
 *
 * Время здесь хранится числом (epoch millis), а не ISO-строкой как в доменных сущностях:
 * это внутренние отметки планировщика, они не участвуют в контракте с сервером, и сравнение
 * `nextAttemptAt <= :now` прямо в SQL получается точным и дешёвым.
 */
@Entity(tableName = "outbox")
data class OutboxEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val sequenceNo: Long = 0,
    val entityType: OutboxEntityType,
    val entityLocalId: String,
    val operation: OutboxOperation,
    val payload: String,
    val idempotencyKey: String,
    val status: OutboxStatus,
    val attemptCount: Int,
    val nextAttemptAt: Long,
    val lastError: String?,
    val createdAt: Long,
    val updatedAt: Long
)
