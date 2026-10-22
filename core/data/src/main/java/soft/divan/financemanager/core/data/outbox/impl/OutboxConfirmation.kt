package soft.divan.financemanager.core.data.outbox.impl

import soft.divan.financemanager.core.data.source.OutboxLocalDataSource
import soft.divan.financemanager.core.database.entity.OutboxEntryEntity
import soft.divan.financemanager.core.database.model.OutboxOperation
import soft.divan.financemanager.core.database.model.SyncStatus

/**
 * Статус локальной строки после того, как сервер принял операцию [entry].
 *
 * `SYNCED` — только если операция последняя в очереди по этой строке. Если за ней стоят ещё
 * (вторая правка, удаление), строка остаётся в [current]: иначе `SYNCED` открыл бы её для
 * перезаписи pull'ом — тот идёт раньше очереди и откатил бы неотправленную правку, — а
 * `PENDING_DELETE` сменился бы на `SYNCED`, и удалённая запись вернулась бы в списки.
 *
 * Вызывается внутри локального эффекта, то есть в транзакции закрытия записи очереди: решение
 * принимается по состоянию очереди на этот момент.
 */
internal suspend fun OutboxLocalDataSource.syncStatusAfter(
    entry: OutboxEntryEntity,
    current: SyncStatus
): SyncStatus =
    if (hasOpenOperations(entry.entityLocalId, exceptSequenceNo = entry.sequenceNo)) {
        current
    } else {
        SyncStatus.SYNCED
    }

/**
 * `serverId` строки после подтверждения операции.
 *
 * Берётся из ответа ([responseId]), иначе остаётся прежним ([current]). Если создание
 * подтвердилось без тела (`204`, пустой ответ конвертера), id всё равно известен: сервер создаёт
 * запись под клиентским id, с которым ушёл `POST`. Без этого строка стала бы `SYNCED` без
 * `serverId`, и следующие правки упали бы в dead-letter с «нет serverId».
 */
internal fun OutboxEntryEntity.confirmedServerId(responseId: String?, current: String?): String? =
    responseId ?: current ?: entityLocalId.takeIf { operation == OutboxOperation.CREATE }
