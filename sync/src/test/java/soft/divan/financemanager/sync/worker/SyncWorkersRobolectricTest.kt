package soft.divan.financemanager.sync.worker

import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SyncWorkersRobolectricTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    /* ---------- SyncNotifications ---------- */

    @Test
    fun `syncForegroundInfo builds notification and registers channel`() {
        val info = context.syncForegroundInfo()

        assertThat(info.notificationId).isEqualTo(1)
        assertThat(info.notification.smallIcon).isNotNull()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager
        val channel = shadowOf(manager).notificationChannels
            .map { it as android.app.NotificationChannel }
            .firstOrNull { it.id == "SyncNotificationChannel" }
        assertThat(channel).isNotNull()
    }

    /* ---------- SyncWorker ---------- */

    private fun buildSyncWorker(coordinator: SyncCoordinator): ListenableWorker =
        TestListenableWorkerBuilder<SyncWorker>(context)
            .setWorkerFactory(object : androidx.work.WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker = SyncWorker(
                    appContext = appContext,
                    workerParams = workerParameters,
                    syncCoordinator = coordinator,
                    ioDispatcher = UnconfinedTestDispatcher()
                )
            })
            .build()

    @Test
    fun `sync worker returns success when coordinator succeeds`() = runTest {
        val coordinator = mockk<SyncCoordinator> { coEvery { syncAll() } returns true }

        val result = (buildSyncWorker(coordinator) as CoroutineWorker).doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun `sync worker retries when coordinator fails`() = runTest {
        val coordinator = mockk<SyncCoordinator> { coEvery { syncAll() } returns false }

        val result = (buildSyncWorker(coordinator) as CoroutineWorker).doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.retry())
    }

    @Test
    fun `sync worker exposes foreground info`() = runTest {
        val coordinator = mockk<SyncCoordinator>()

        val info = (buildSyncWorker(coordinator) as CoroutineWorker).getForegroundInfo()

        assertThat(info.notificationId).isEqualTo(1)
    }
}
