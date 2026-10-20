package soft.divan.financemanager.core.data.sync

import soft.divan.financemanager.core.domain.error.DomainError

/**
 * Считается ли сетевая ошибка pull'а успехом шага синхронизации.
 *
 * Да — только для сети, заблокированной состоянием клиента: гость
 * ([DomainError.GuestModeBlocked]) или нет сессии ([DomainError.Unauthorized]). Повтор работы
 * этого не изменит, а `Result.retry()` гонял бы воркер вхолостую, пока пользователь не войдёт.
 * Так же очередь исходящих операций трактует `OutboxSendResult.Blocked`. Остальное (нет сети,
 * `5xx`, непредвиденный сбой) — сбой шага: координатор не обновит время синхронизации и попросит
 * повтор.
 */
internal fun DomainError.isPullSkipped(): Boolean =
    this is DomainError.GuestModeBlocked || this is DomainError.Unauthorized
