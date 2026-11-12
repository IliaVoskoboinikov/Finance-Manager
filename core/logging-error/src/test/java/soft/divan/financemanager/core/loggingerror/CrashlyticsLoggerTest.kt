package soft.divan.financemanager.core.loggingerror

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
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
        // Ветка logcat задана явно, а не через BuildConfig варианта сборки
        logger = CrashlyticsLogger(context, logFullText = true)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `recordError with throwable sends type and stack without text`() {
        val sent = slot<Throwable>()
        every { crashlytics.recordException(capture(sent), any()) } returns Unit
        // Текст исключения пишет библиотека, и в нём может оказаться значение из ответа
        val boom = IllegalStateException(
            "Failed parsing '1234.56' as BigDecimal",
            NumberFormatException("Number has unsupported scale: 1234.56")
        )

        logger.recordError(boom)

        val chain = generateSequence(sent.captured) { it.cause }.toList()
        assertThat(chain.map { it.message }).containsExactly(
            IllegalStateException::class.java.name,
            NumberFormatException::class.java.name
        )
        assertThat(chain.map { it.stackTrace.toList() })
            .containsExactly(boom.stackTrace.toList(), boom.cause?.stackTrace?.toList())
        verify(exactly = 0) { crashlytics.log(any()) }
    }

    @Test
    fun `recordError with throwable and message logs context first`() {
        val boom = IllegalStateException("boom")

        logger.recordError(boom, "while syncing accounts")

        verify(exactly = 1) { crashlytics.log("while syncing accounts") }
        verify(exactly = 1) {
            crashlytics.recordException(
                match { it.message == IllegalStateException::class.java.name },
                any()
            )
        }
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

        verify(exactly = 1) { Log.e(any(), "while syncing accounts", any()) }
        verify(exactly = 1) { Log.e(any(), "invalid server payload") }
        verify(exactly = 0) { FirebaseCrashlytics.getInstance() }
    }

    @Test
    fun `debug logcat keeps the full exception`() {
        val boom = IllegalStateException("boom")

        CrashlyticsLogger(context, logFullText = true).recordError(boom)

        verify(exactly = 1) { Log.e(any(), "boom", boom) }
        // Полный текст — только в logcat: в Crashlytics всё равно уходит копия
        verify(exactly = 1) { crashlytics.recordException(match { it is RedactedException }, any()) }
    }

    @Test
    fun `release logcat gets the copy without text`() {
        val boom = IllegalStateException("Failed parsing '1234.56' as BigDecimal")

        CrashlyticsLogger(context, logFullText = false).recordError(boom)

        verify(exactly = 1) {
            Log.e(
                any(),
                IllegalStateException::class.java.name,
                match { it is RedactedException && it.message == IllegalStateException::class.java.name }
            )
        }
    }

    @Test
    fun `release logcat keeps the caller context as is`() {
        CrashlyticsLogger(context, logFullText = false)
            .recordError(IllegalStateException("1234.56"), "Sync: pull transactions")

        verify(exactly = 1) {
            Log.e(any(), "Sync: pull transactions", match { it is RedactedException })
        }
    }

    @Test
    fun `failing redaction still reports the type without text`() {
        val sent = slot<Throwable>()
        every { crashlytics.recordException(capture(sent), any()) } returns Unit
        // Чужой Throwable, у которого цепочка cause сама бросает
        val hostile = object : IllegalStateException("1234.56") {
            override val cause: Throwable get() = error("broken cause")
        }

        logger.recordError(hostile)

        assertThat(sent.captured).isInstanceOf(RedactedException::class.java)
        assertThat(sent.captured.message).isEqualTo(hostile.javaClass.name).doesNotContain("1234.56")
    }
}
