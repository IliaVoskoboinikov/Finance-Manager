package soft.divan.financemanager.feature.haptics.impl.data.source.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HapticsLocalSourceImplTest {

    private val dataStore = mockk<DataStore<Preferences>>()
    private val localSource = HapticsLocalSourceImpl(dataStore)

    private val key = booleanPreferencesKey("app_haptics_enabled")

    @Test
    fun `getHapticsEnabled returns stored value`() = runTest {
        every { dataStore.data } returns flowOf(preferencesOf(key to false))

        assertThat(localSource.getHapticsEnabled().first()).isFalse()
    }

    @Test
    fun `getHapticsEnabled defaults to true when nothing stored`() = runTest {
        every { dataStore.data } returns flowOf(emptyPreferences())

        assertThat(localSource.getHapticsEnabled().first()).isTrue()
    }

    @Test
    fun `setHapticsEnabled stores value under haptics key`() = runTest {
        val transform = slot<suspend (Preferences) -> Preferences>()
        coEvery { dataStore.updateData(capture(transform)) } coAnswers {
            transform.captured(emptyPreferences())
        }

        localSource.setHapticsEnabled(false)

        val stored = transform.captured(emptyPreferences())
        assertThat(stored[key]).isFalse()
    }
}
