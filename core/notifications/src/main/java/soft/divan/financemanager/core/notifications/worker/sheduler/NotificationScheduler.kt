package soft.divan.financemanager.core.notifications.worker.sheduler

interface NotificationScheduler {
    /**
     * Schedules a periodic work to notify the user if they haven't opened the app for 7 days.
     * This work is unique and will be replaced on each call to reset the timer.
     */
    fun scheduleInactivityNotification()
}
