package soft.divan.financemanager.core.data.outbox

import soft.divan.financemanager.core.database.entity.OutboxEntryEntity

/**
 * Исход попытки отправить одну запись очереди.
 *
 * Классификация важнее самого факта ошибки: от неё зависит, повторим ли мы операцию, потратим ли
 * на неё попытку или уведём в dead-letter. Ошибка «сервер прилёг» и ошибка «сервер отверг данные»
 * требуют противоположного поведения.
 */
sealed interface OutboxSendResult {

    /**
     * Операция применена на сервере.
     *
     * Сюда же относится идемпотентный повтор — «уже существует» для создания и «уже нет» для
     * удаления: цель операции достигнута, даже если фактическую работу выполнила прошлая попытка.
     */
    data object Success : OutboxSendResult

    /** Временный сбой (сеть, 5xx): повторяем позже с возрастающей паузой. */
    data class Transient(val reason: String) : OutboxSendResult

    /**
     * Запрос не дошёл до сервера, потому что сеть заблокирована намеренно: гостевой режим или
     * отсутствующая сессия.
     *
     * Отличается от [Transient] тем, что **не тратит попытку**: пользователь не виноват, что ещё
     * не вошёл, и запись не должна из-за этого попасть в dead-letter.
     */
    data class Blocked(val reason: String) : OutboxSendResult

    /** Сервер отверг операцию по существу (валидация): повтор ничего не изменит. */
    data class Terminal(val reason: String) : OutboxSendResult
}

/**
 * Умеет выполнить одну операцию из очереди: разобрать снимок запроса и сходить в нужный эндпоинт.
 *
 * Отделён от [OutboxProcessor], чтобы механика очереди (порядок, захват, повторы, dead-letter)
 * не зависела от знания об эндпоинтах и могла проверяться изолированно.
 */
interface OutboxSender {
    suspend fun send(entry: OutboxEntryEntity): OutboxSendResult
}
