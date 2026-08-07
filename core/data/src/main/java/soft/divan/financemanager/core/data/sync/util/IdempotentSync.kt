package soft.divan.financemanager.core.data.sync.util

import soft.divan.financemanager.core.domain.error.DomainError

/**
 * Сервер ответил «такой записи нет» (HTTP 404 → [DomainError.NoData]).
 *
 * Для удаления это **идемпотентный успех**: цель операции — отсутствие записи на сервере — уже
 * достигнута, скорее всего предыдущей попыткой, ACK которой не дошёл. Без такой трактовки запись
 * навсегда осталась бы в `PENDING_DELETE` и бесполезно ретраилась каждым циклом синка.
 *
 * Подробнее: [docs/idempotency.md](../../../../../../../../../../docs/idempotency.md).
 */
internal fun DomainError.isNotFound(): Boolean = this is DomainError.NoData

/**
 * Запрос не дошёл до сервера, потому что сеть заблокирована намеренно: гостевой режим или
 * отсутствующая/протухшая сессия.
 *
 * В этом случае перепроверять состояние сервера бессмысленно — read-back упрётся в ту же
 * блокировку. Запись остаётся `PENDING_*` до логина, фоновый синк повторит её позже.
 */
internal fun DomainError.isNetworkBlocked(): Boolean =
    this is DomainError.GuestModeBlocked || this is DomainError.Unauthorized
