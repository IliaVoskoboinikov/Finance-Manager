package soft.divan.financemanager.core.workmanager

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.test.core.app.ApplicationProvider
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import dagger.hilt.android.EntryPointAccessors
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DelegatingWorkerTest {

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

    private class RecordingWorker(
        context: Context,
        params: WorkerParameters
    ) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result = Result.success()
    }

    @Test
    fun `delegatedData stores the delegate class name`() {
        val data = RecordingWorker::class.delegatedData()

        assertThat(data.getString(WORKER_CLASS_NAME))
            .isEqualTo(RecordingWorker::class.java.name)
    }

    @Test
    fun `delegating worker delegates doWork to worker from hilt factory`() = runTest {
        val hiltWorkerFactory = mockk<HiltWorkerFactory>()
        every {
            hiltWorkerFactory.createWorker(any(), any(), any())
        } answers { RecordingWorker(firstArg(), thirdArg()) }
        stubHiltEntryPoint(hiltWorkerFactory)

        val worker = TestListenableWorkerBuilder<DelegatingWorker>(context)
            .setInputData(RecordingWorker::class.delegatedData())
            .build()

        assertThat(worker.doWork()).isEqualTo(ListenableWorker.Result.success())
    }

    @Test
    fun `delegating worker delegates getForegroundInfo to the delegate`() = runTest {
        val delegate = mockk<CoroutineWorker>(relaxed = true)
        val hiltWorkerFactory = mockk<HiltWorkerFactory>()
        every { hiltWorkerFactory.createWorker(any(), any(), any()) } returns delegate
        stubHiltEntryPoint(hiltWorkerFactory)

        val worker = TestListenableWorkerBuilder<DelegatingWorker>(context)
            .setInputData(RecordingWorker::class.delegatedData())
            .build()

        worker.getForegroundInfo()

        coVerify { delegate.getForegroundInfo() }
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
