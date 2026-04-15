package soft.divan.financemanager.core.notifications

import soft.divan.financemanager.core.notifications.model.NotificationMessage

interface NotificationHelper {
    /**
     * Shows a notification based on the provided [NotificationMessage].
     */
    fun showNotification(message: NotificationMessage)

    /**
     * Retrieves the current Firebase Cloud Messaging registration token.
     * @param onTokenReceived Callback with the token string or null if failed.
     */
    fun getFcmToken(onTokenReceived: (String?) -> Unit)
}
