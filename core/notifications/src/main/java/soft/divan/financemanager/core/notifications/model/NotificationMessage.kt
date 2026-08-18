package soft.divan.financemanager.core.notifications.model

import androidx.annotation.DrawableRes

/**
 * Готовое к показу уведомление.
 *
 * @param id идентификатор в системной шторке. Уведомления с одинаковым [id] заменяют
 *   друг друга — поэтому id задаётся явно (см. [NotificationIds]), а не генерируется
 *   из времени: иначе каждый показ плодил бы новую строку в шторке.
 * @param iconRes иконка в статус-баре; `null` — взять иконку приложения.
 */
data class NotificationMessage(
    val id: Int,
    val title: String,
    val body: String,
    @param:DrawableRes val iconRes: Int? = null
)

/**
 * Идентификаторы уведомлений приложения.
 *
 * Держим в одном месте, чтобы каналы не начали затирать друг друга. `1` занят
 * foreground-уведомлением синхронизации (`:sync`, `SyncNotifications.kt`).
 */
object NotificationIds {
    /** Напоминание о неактивности. Одно на всё приложение — новое заменяет старое. */
    const val INACTIVITY_REMINDER = 1001

    /** Пуш без собственного идентификатора. */
    const val PUSH_FALLBACK = 1002
}
