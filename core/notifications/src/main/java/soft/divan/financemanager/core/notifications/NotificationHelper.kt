package soft.divan.financemanager.core.notifications

import soft.divan.financemanager.core.notifications.model.NotificationMessage

/**
 * Показ пользовательских уведомлений приложения.
 *
 * Реализация сама создаёт канал и молча пропускает показ, если разрешение
 * `POST_NOTIFICATIONS` не выдано, — вызывающему не нужно это проверять.
 */
interface NotificationHelper {

    /** Показывает (или заменяет — по [NotificationMessage.id]) уведомление. */
    fun showNotification(message: NotificationMessage)
}
