package soft.divan.financemanager.core.loggingerror

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test

class CrashlyticsLoggerTest {

    private val context = mockk<Context>()
    private val crashlytics = mockk<FirebaseCrashlytics>(relaxed = true)

    private lateinit var logger: CrashlyticsLogger

    @Before
    fun setUp() {
        mockkStatic(FirebaseApp::class)
        every { FirebaseApp.getApps(context) } returns listOf(mockk())
        mockkStatic(FirebaseCrashlytics::class)
        every { FirebaseCrashlytics.getInstance() } returns crashlytics
        mockkStatic(Log::class)
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.e(any(), any()) } returns 0
        logger = CrashlyticsLogger(context)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `recordError with throwable sends original exception`() {
        val boom = IllegalStateException("boom")

        logger.recordError(boom)

        verify(exactly = 1) { crashlytics.recordException(boom) }
        verify(exactly = 0) { crashlytics.log(any()) }
    }

    @Test
    fun `recordError with throwable and message logs context first`() {
        val boom = IllegalStateException("boom")

        logger.recordError(boom, "while syncing accounts")

        verify(exactly = 1) { crashlytics.log("while syncing accounts") }
        verify(exactly = 1) { crashlytics.recordException(boom) }
    }

    @Test
    fun `recordError with message wraps it into named exception`() {
        logger.recordError("invalid server payload")

        verify(exactly = 1) {
            crashlytics.recordException(
                match { it !is IllegalStateException && it.message == "invalid server payload" }
            )
        }
    }

    @Test
    fun `recordError implements ErrorLogger contract`() {
        val errorLogger: ErrorLogger = logger

        assertThat(errorLogger).isInstanceOf(CrashlyticsLogger::class.java)
    }

    @Test
    fun `crashlytics is not resolved on construction`() {
        CrashlyticsLogger(context)

        // getInstance() без FirebaseApp бросает: дёрнуть его при сборке Hilt-графа — уронить процесс
        verify(exactly = 0) { FirebaseCrashlytics.getInstance() }
    }

    @Test
    fun `without FirebaseApp errors go to logcat only`() {
        every { FirebaseApp.getApps(context) } returns emptyList()
        val boom = IllegalStateException("boom")

        logger.recordError(boom, "while syncing accounts")
        logger.recordError("invalid server payload")

        verify(exactly = 1) { Log.e(any(), "while syncing accounts", boom) }
        verify(exactly = 1) { Log.e(any(), "invalid server payload") }
        verify(exactly = 0) { FirebaseCrashlytics.getInstance() }
    }
}
