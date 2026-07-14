package soft.divan.financemanager.core.network.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
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
        val capabilities = Shadow.newInstanceOf(NetworkCapabilities::class.java)
        shadowOf(capabilities).addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        shadowOf(connectivityManager).setNetworkCapabilities(
            connectivityManager.activeNetwork,
            capabilities
        )

        val monitor = ConnectivityManagerNetworkMonitor(context)

        assertThat(monitor.isOnline.first()).isTrue()
    }

    @Test
    fun `network callback drives online state changes`() = runBlocking<Unit> {
        val capabilities = Shadow.newInstanceOf(NetworkCapabilities::class.java)
        shadowOf(capabilities).addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        shadowOf(connectivityManager).setNetworkCapabilities(
            connectivityManager.activeNetwork,
            capabilities
        )
        val monitor = ConnectivityManagerNetworkMonitor(context)
        val values = mutableListOf<Boolean>()

        val job = launch(Dispatchers.Default) { monitor.isOnline.collect { values += it } }
        withTimeout(5000) {
            while (shadowOf(connectivityManager).networkCallbacks.isEmpty()) {
                kotlinx.coroutines.delay(10)
            }
        }
        val callback = shadowOf(connectivityManager).networkCallbacks.first()
        val network = connectivityManager.activeNetwork!!

        callback.onAvailable(network)
        callback.onLost(network)
        withTimeout(5000) {
            while (values.lastOrNull() != false) {
                kotlinx.coroutines.delay(10)
            }
        }
        job.cancel()

        // после onLost сеть считается потерянной
        assertThat(values.last()).isFalse()
        assertThat(values).contains(true)
    }
}
