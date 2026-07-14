package soft.divan.financemanager.feature.haptics.impl.di

import android.content.Context
import android.os.Vibrator
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.test.core.app.ApplicationProvider
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import soft.divan.financemanager.feature.haptics.impl.data.haptics.HapticsManagerImpl
import soft.divan.financemanager.feature.haptics.impl.data.source.impl.HapticsLocalSourceImpl
import soft.divan.financemanager.feature.haptics.impl.domain.usecase.ObserveHapticsEnabledUseCase

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class HapticsModuleTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    @Config(sdk = [35])
    fun `provideVibrator resolves default vibrator via VibratorManager on API 31+`() {
        assertThat(HapticsModule.provideVibrator(context)).isInstanceOf(Vibrator::class.java)
    }

    @Test
    @Config(sdk = [30])
    fun `provideVibrator resolves vibrator service below API 31`() {
        assertThat(HapticsModule.provideVibrator(context)).isInstanceOf(Vibrator::class.java)
    }

    @Test
    @Config(sdk = [35])
    fun `provideDataStore returns a preferences datastore`() {
        assertThat(HapticsModule.provideDataStore(context)).isNotNull()
    }

    @Test
    fun `provideHapticsPreferences builds datastore-backed source`() {
        val source = HapticsModule.provideHapticsPreferences(mockk<DataStore<Preferences>>())

        assertThat(source).isInstanceOf(HapticsLocalSourceImpl::class.java)
    }

    @Test
    fun `provideHapticsManager builds manager subscribed to settings`() {
        val observe = mockk<ObserveHapticsEnabledUseCase> {
            every { this@mockk() } returns flowOf(true)
        }

        val manager = HapticsModule.provideHapticsManager(
            vibrator = mockk<Vibrator>(),
            observeHapticsEnabled = observe,
            scope = CoroutineScope(UnconfinedTestDispatcher())
        )

        assertThat(manager).isInstanceOf(HapticsManagerImpl::class.java)
    }
}
