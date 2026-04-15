package soft.divan.financemanager

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import soft.divan.common.di.ApplicationScope
import soft.divan.financemanager.core.notifications.worker.sheduler.NotificationScheduler
import soft.divan.financemanager.sync.initializers.SyncInitializer
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    @Inject
    lateinit var syncInitializer: SyncInitializer

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.Default).launch {
            syncInitializer.initialize()
            notificationScheduler.scheduleInactivityNotification()
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        applicationScope.cancel()
    }
}
