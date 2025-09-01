package soft.divan.financemanager

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import soft.divan.financemanager.sync.init.Sync
import javax.inject.Inject

@HiltAndroidApp
class App: Application() {

    @Inject
    lateinit var applicationScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        Sync.initialize(context = this)
    }

    override fun onTerminate() {
        super.onTerminate()
        applicationScope.cancel()
    }
}
