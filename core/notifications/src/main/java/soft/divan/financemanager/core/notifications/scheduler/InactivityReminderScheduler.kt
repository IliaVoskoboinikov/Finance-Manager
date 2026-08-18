package soft.divan.financemanager.core.notifications.scheduler

/**
 * Напоминание «вы давно не заходили».
 *
 * Отсчёт ведётся от последнего реального появления приложения на переднем плане,
 * поэтому единственный метод — [onUserActive], который вызывается из
 * `ProcessLifecycleOwner.ON_START`.
 */
interface InactivityReminderScheduler {

    /**
     * Сдвигает напоминание на [INACTIVITY_THRESHOLD_DAYS] дней вперёд.
     * Идемпотентен: повторные вызовы просто переставляют таймер.
     */
    fun onUserActive()

    /** Снимает запланированное напоминание. */
    fun cancel()
}

/** Через столько дней без запуска приложения показываем напоминание. */
const val INACTIVITY_THRESHOLD_DAYS = 7L
