package soft.divan.financemanager

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import javax.inject.Inject

@HiltAndroidApp
class App: Application() {

    @Inject
    lateinit var applicationScope: CoroutineScope

    override fun onTerminate() {
        super.onTerminate()
        applicationScope.cancel()
    }
}