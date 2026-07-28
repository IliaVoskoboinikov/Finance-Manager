package soft.divan.financemanager.sync.worker

import android.app.NotificationManager
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.test.core.app.ApplicationProvider
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import dagger.hilt.android.EntryPointAccessors
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
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

    @After
    fun tearDown() {
        unmockkStatic(EntryPointAccessors::class)
    }

    private fun stubHiltEntryPoint(factory: HiltWorkerFactory) {
        mockkStatic(EntryPointAccessors::class)
        every {
            EntryPointAccessors.fromApplication(
                any<Context>(),
                HiltWorkerFactoryEntryPoint::class.java
            )
        } returns object : HiltWorkerFactoryEntryPoint {
            override fun hiltWorkerFactory(): HiltWorkerFactory = factory
        }
    }

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

    /* ---------- DelegatingWorker ---------- */

    private class RecordingWorker(
        context: Context,
        params: WorkerParameters
    ) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result = Result.success()
    }

    @Test
    fun `delegating worker delegates doWork to worker from hilt factory`() = runTest {
        val hiltWorkerFactory = mockk<HiltWorkerFactory>()
        every {
            hiltWorkerFactory.createWorker(any(), any(), any())
        } answers { RecordingWorker(firstArg(), thirdArg()) }
        stubHiltEntryPoint(hiltWorkerFactory)

        val worker = TestListenableWorkerBuilder<DelegatingWorker>(context)
            .setInputData(SyncWorker::class.delegatedData())
            .build()

        assertThat(worker.doWork()).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun `delegating worker fails fast when delegate cannot be created`() {
        val hiltWorkerFactory = mockk<HiltWorkerFactory>()
        every { hiltWorkerFactory.createWorker(any(), any(), any()) } returns null
        stubHiltEntryPoint(hiltWorkerFactory)

        val thrown = runCatching {
            TestListenableWorkerBuilder<DelegatingWorker>(context).build()
        }.exceptionOrNull()

        // билдер создаёт воркер рефлексией, поэтому исходное исключение — в root cause
        assertThat(thrown).hasRootCauseInstanceOf(IllegalArgumentException::class.java)
    }
}
