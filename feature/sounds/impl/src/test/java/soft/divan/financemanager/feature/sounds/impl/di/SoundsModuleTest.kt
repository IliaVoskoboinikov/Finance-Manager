package soft.divan.financemanager.feature.sounds.impl.di

import android.content.Context
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
import soft.divan.financemanager.feature.sounds.impl.data.sounds.SoundPlayerImpl
import soft.divan.financemanager.feature.sounds.impl.data.sounds.SoundsPoolHolder
import soft.divan.financemanager.feature.sounds.impl.data.source.impl.SoundsLocalSourceImpl
import soft.divan.financemanager.feature.sounds.impl.domain.usecase.ObserveSoundsEnabledUseCase

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class SoundsModuleTest {

    @Test
    fun `provideSoundsDataStore returns a preferences datastore`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        assertThat(SoundsModule.provideSoundsDataStore(context)).isNotNull()
    }

    @Test
    fun `provideSoundsPoolHolder builds a pool holder`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        assertThat(SoundsModule.provideSoundsPoolHolder(context)).isNotNull()
    }

    @Test
    fun `provideSoundsPreferences builds datastore-backed source`() {
        val source = SoundsModule.provideSoundsPreferences(mockk<DataStore<Preferences>>())

        assertThat(source).isInstanceOf(SoundsLocalSourceImpl::class.java)
    }

    @Test
    fun `provideSoundsPlayer builds player subscribed to settings`() {
        val observe = mockk<ObserveSoundsEnabledUseCase> {
            every { this@mockk() } returns flowOf(true)
        }

        val player = SoundsModule.provideSoundsPlayer(
            soundsPoolHolder = mockk<SoundsPoolHolder>(),
            observeSoundEnabled = observe,
            scope = CoroutineScope(UnconfinedTestDispatcher())
        )

        assertThat(player).isInstanceOf(SoundPlayerImpl::class.java)
    }
}
