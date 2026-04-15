package soft.divan.financemanager.core.notifications.model

data class NotificationMessage(
    val id: Int = System.currentTimeMillis().toInt(),
    val title: String,
    val body: String,
    val deepLink: String? = null,
    val iconRes: Int? = null
)
