package soft.divan.financemanager.sync.scheduler

interface SyncScheduler {
    fun schedulePeriodicSync(hours: Int)
    fun scheduleOneTimeSync()
}