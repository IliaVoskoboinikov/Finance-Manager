package soft.divan.financemanager.core.database.entity

import androidx.room.Entity
import androidx.room.Index
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
 * @property dependencyKey `localId` сущности, которая обязана появиться на сервере **раньше**
 *   этой операции. Счёт кладёт сюда собственный `localId` (он ни от кого не зависит), транзакция —
 *   `localId` своего счёта: сервер отвергает транзакцию с неизвестным `accountId`.
 *
 *   Это ссылка на предшественника, а не «номер группы»: транзакции одного счёта ждут **счёт**, но
 *   не друг друга. Полный порядок задают три ребра — «после прошлой операции той же строки»,
 *   «после своего предшественника» и «после тех, кто зависит от меня» (удаление счёта не должно
 *   обгонять его транзакции); все три проверяются в `OutboxDao.getReadyToSend`.
 *
 *   Без этого поля порядок держался только на `sequenceNo`, а он ломался фильтром готовности:
 *   операция, ушедшая в backoff или под аренду, выпадала из выборки, и следующая операция **той
 *   же сущности** обгоняла её (правка уезжала раньше создания и умирала с `404`).
 * @property operation Что именно делаем на сервере.
 * @property targetServerId Идентификатор ресурса на сервере — адрес для `PUT`/`DELETE`
 *   (`null` для [OutboxOperation.CREATE], где ресурса ещё нет). Хранится в записи, а не
 *   вычисляется при отправке, чтобы операция оставалась самодостаточной: её можно выполнить,
 *   даже если доменной строки уже нет локально (например, повтор после удаления).
 * @property payload Снимок тела запроса (JSON) на момент операции. Хранится готовым, чтобы
 *   отправка не зависела от последующих правок доменной строки: очередь — это журнал событий,
 *   а не указатель на текущее состояние. Для операций без тела (`DELETE`) — пустой объект `{}`.
 * @property idempotencyKey Ключ дедупликации **этой операции** — новый UUID на каждую постановку
 *   в очередь. Стабилен на все попытки отправки (генерируется один раз и хранится вместе с
 *   записью), но различает операции: у создания строки и её последующей правки ключи разные,
 *   иначе сервер счёл бы правку повтором создания и молча её проглотил. Не путать с
 *   [entityLocalId] — тот адресует **сущность**, а этот **намерение её изменить**.
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
@Entity(
    tableName = "outbox",
    indices = [
        // Барьер порядка ищет незакрытых предшественников по entityLocalId — без индекса это
        // полное сканирование очереди на каждую строку выборки.
        Index(value = ["entityLocalId", "status"]),
        // Каскад dead-letter находит по dependencyKey тех, кто ждал провалившуюся операцию.
        Index(value = ["dependencyKey", "status"])
    ]
)
data class OutboxEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val sequenceNo: Long = 0,
    val entityType: OutboxEntityType,
    val entityLocalId: String,
    val dependencyKey: String,
    val operation: OutboxOperation,
    val targetServerId: String?,
    val payload: String,
    val idempotencyKey: String,
    val status: OutboxStatus,
    val attemptCount: Int,
    val nextAttemptAt: Long,
    val lastError: String?,
    val createdAt: Long,
    val updatedAt: Long
)
