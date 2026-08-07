package soft.divan.financemanager.core.database.model

/**
 * Жизненный цикл записи outbox.
 *
 * ```
 * PENDING ──берём в работу──> IN_PROGRESS ──успех──────────> COMPLETED
 *    ^                             │
 *    └──── transient-ошибка ───────┤  (attemptCount++, nextAttemptAt = backoff)
 *                                  └──── terminal-ошибка ──> FAILED (dead-letter)
 * ```
 *
 * Разделение [PENDING] и [IN_PROGRESS] защищает от двойной отправки, если процессор запустится
 * повторно (например, WorkManager и оппортунистический прогон пересеклись).
 *
 * [FAILED] — терминальное состояние: ретраи прекращены, запись видна для диагностики и ручного
 * повтора. Туда попадают ошибки, которые повтор не исправит (валидация), и записи, исчерпавшие
 * лимит попыток.
 */
enum class OutboxStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED
}
