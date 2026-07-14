package soft.divan.financemanager.feature.sounds.impl.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.feature.sounds.impl.data.repository.SoundsRepositoryImpl
import soft.divan.financemanager.feature.sounds.impl.data.source.SoundsLocalSource
import soft.divan.financemanager.feature.sounds.impl.data.source.impl.SoundsLocalSourceImpl

class SoundsDataLayerTest {

    /* ---------- SoundsRepositoryImpl ---------- */

    private val localSource = mockk<SoundsLocalSource>(relaxUnitFun = true)
    private val repository = SoundsRepositoryImpl(localSource)

    @Test
    fun `observeSoundsEnabled delegates to local source`() = runTest {
        every { localSource.getSoundEnabled() } returns flowOf(false)

        assertThat(repository.observeSoundsEnabled().first()).isFalse()
    }

    @Test
    fun `setSoundsEnabled delegates to local source`() = runTest {
        repository.setSoundsEnabled(true)

        coVerify(exactly = 1) { localSource.setSoundEnabled(true) }
    }

    /* ---------- SoundsLocalSourceImpl ---------- */

    private val dataStore = mockk<DataStore<Preferences>>()
    private val dataSource = SoundsLocalSourceImpl(dataStore)
    private val key = booleanPreferencesKey("app_sound_enabled")

    @Test
    fun `getSoundEnabled returns stored value`() = runTest {
        every { dataStore.data } returns flowOf(preferencesOf(key to false))

        assertThat(dataSource.getSoundEnabled().first()).isFalse()
    }

    @Test
    fun `getSoundEnabled defaults to true`() = runTest {
        every { dataStore.data } returns flowOf(emptyPreferences())

        assertThat(dataSource.getSoundEnabled().first()).isTrue()
    }

    @Test
    fun `setSoundEnabled stores value under sound key`() = runTest {
        val transform = slot<suspend (Preferences) -> Preferences>()
        coEvery { dataStore.updateData(capture(transform)) } coAnswers {
            transform.captured(emptyPreferences())
        }

        dataSource.setSoundEnabled(false)

        assertThat(transform.captured(emptyPreferences())[key]).isFalse()
    }
}
