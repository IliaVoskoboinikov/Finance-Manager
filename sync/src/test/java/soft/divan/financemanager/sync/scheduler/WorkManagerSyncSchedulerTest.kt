package soft.divan.financemanager.sync.scheduler

import android.content.Context
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import soft.divan.financemanager.sync.initializers.SYNC_ONE_TIME_WORK
import soft.divan.financemanager.sync.initializers.SYNC_PERIODIC_WORK
import soft.divan.financemanager.sync.worker.WORKER_CLASS_NAME

class WorkManagerSyncSchedulerTest {

    private val context = mockk<Context>()
    private val workManager = mockk<WorkManager>(relaxed = true)

    private val scheduler = WorkManagerSyncScheduler(context)

    @Before
    fun setUp() {
        mockkObject(WorkManager.Companion)
        every { WorkManager.getInstance(context) } returns workManager
        mockkStatic(Log::class)
        every { Log.d(any(), any()) } returns 0
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `schedulePeriodicSync enqueues unique periodic work with KEEP policy`() {
        val request = slot<PeriodicWorkRequest>()
        every {
            workManager.enqueueUniquePeriodicWork(
                SYNC_PERIODIC_WORK,
                ExistingPeriodicWorkPolicy.KEEP,
                capture(request)
            )
        } returns mockk()

        scheduler.schedulePeriodicSync(6)

        val spec = request.captured.workSpec
        assertThat(spec.intervalDuration).isEqualTo(6 * 60 * 60 * 1000L)
        assertThat(spec.constraints.requiredNetworkType).isEqualTo(NetworkType.CONNECTED)
        assertThat(spec.input.getString(WORKER_CLASS_NAME))
            .contains("SyncWorker")
    }

    @Test
    fun `scheduleOneTimeSync enqueues unique work with REPLACE policy`() {
        val request = slot<OneTimeWorkRequest>()
        every {
            workManager.enqueueUniqueWork(
                SYNC_ONE_TIME_WORK,
                ExistingWorkPolicy.REPLACE,
                capture(request)
            )
        } returns mockk()

        scheduler.scheduleOneTimeSync()

        val spec = request.captured.workSpec
        assertThat(spec.constraints.requiredNetworkType).isEqualTo(NetworkType.CONNECTED)
        assertThat(spec.input.getString(WORKER_CLASS_NAME)).contains("SyncWorker")
    }
}
