package soft.divan.financemanager

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import soft.divan.common.di.ApplicationScope
import soft.divan.common.di.IoDispatcher
import soft.divan.financemanager.core.domain.usecase.InitializeLocalDataUseCase
import soft.divan.financemanager.core.network.util.LegacyHttpCache
import soft.divan.financemanager.lifecycle.LocalDataAutoLock
import soft.divan.financemanager.sync.initializers.SyncInitializer
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    @Inject
    @IoDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    @Inject
    lateinit var syncInitializer: SyncInitializer

    @Inject
    lateinit var initializeLocalData: InitializeLocalDataUseCase

    @Inject
    lateinit var localDataAutoLock: LocalDataAutoLock

    @Inject
    lateinit var legacyHttpCache: LegacyHttpCache

    override fun onCreate() {
        super.onCreate()
        // Ключи читаются до всего остального: пока они не прочитаны, база закрыта, и главный
        // экран не строится. В фоновом процессе (запуск ради WorkManager) это тоже нужно — синк
        // ждёт конца инициализации.
        applicationScope.launch { initializeLocalData() }
        ProcessLifecycleOwner.get().lifecycle.addObserver(localDataAutoLock)
        applicationScope.launch(ioDispatcher) { legacyHttpCache.delete() }
        applicationScope.launch(Dispatchers.Default) {
            syncInitializer.initialize()
        }
    }
}
