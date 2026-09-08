package soft.divan.financemanager.core.network.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadow.api.Shadow

/** Запас на медленный CI: обмен значениями идёт между потоками, а не в виртуальном времени. */
private const val AWAIT_TIMEOUT_MS = 10_000L

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class ConnectivityManagerNetworkMonitorTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    @Test
    fun `emits false when connectivity manager is unavailable`() = runBlocking<Unit> {
        val brokenContext = mockk<Context> {
            every { getSystemService(ConnectivityManager::class.java) } returns null
        }

        val monitor = ConnectivityManagerNetworkMonitor(brokenContext)

        assertThat(monitor.isOnline.first()).isFalse()
    }

    @Test
    fun `emits false when active network has no internet capability`() = runBlocking<Unit> {
        shadowOf(connectivityManager).setNetworkCapabilities(
            connectivityManager.activeNetwork,
            Shadow.newInstanceOf(NetworkCapabilities::class.java)
        )

        val monitor = ConnectivityManagerNetworkMonitor(context)

        assertThat(monitor.isOnline.first()).isFalse()
    }

    @Test
    fun `emits true when active network has internet capability`() = runBlocking<Unit> {
        givenActiveNetworkWithInternet()

        val monitor = ConnectivityManagerNetworkMonitor(context)

        assertThat(monitor.isOnline.first()).isTrue()
    }

    @Test
    fun `network callback drives online state changes`() = runBlocking<Unit> {
        givenActiveNetworkWithInternet()

        val monitor = ConnectivityManagerNetworkMonitor(context)
        val emissions = Channel<Boolean>(Channel.UNLIMITED)
        val collectJob = launch(Dispatchers.Default) { monitor.isOnline.collect(emissions::send) }

        try {
            // Первое значение уходит в канал уже после registerNetworkCallback, поэтому
            // после его получения callback точно зарегистрирован и виден этому потоку.
            assertThat(emissions.awaitValue(expected = true)).isTrue()

            val callback = shadowOf(connectivityManager).networkCallbacks.first()
            val network = connectivityManager.activeNetwork!!

            callback.onLost(network)
            assertThat(emissions.awaitValue(expected = false)).isFalse()

            callback.onAvailable(network)
            assertThat(emissions.awaitValue(expected = true)).isTrue()
        } finally {
            collectJob.cancel()
        }
    }

    private fun givenActiveNetworkWithInternet() {
        val capabilities = Shadow.newInstanceOf(NetworkCapabilities::class.java)
        shadowOf(capabilities).addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        shadowOf(connectivityManager).setNetworkCapabilities(
            connectivityManager.activeNetwork,
            capabilities
        )
    }

    /**
     * Ждёт значение [expected], пропуская всё до него.
     *
     * `isOnline` завершается `conflate()`, поэтому количество промежуточных эмиссий
     * недетерминировано (медленный коллектор просто не увидит часть значений) — а вот
     * порядок смены состояний детерминирован. Если значение не пришло за
     * [AWAIT_TIMEOUT_MS], тест падает по таймауту.
     */
    private suspend fun ReceiveChannel<Boolean>.awaitValue(expected: Boolean): Boolean =
        withTimeout(AWAIT_TIMEOUT_MS) {
            var value = receive()
            while (value != expected) {
                value = receive()
            }
            value
        }
}
