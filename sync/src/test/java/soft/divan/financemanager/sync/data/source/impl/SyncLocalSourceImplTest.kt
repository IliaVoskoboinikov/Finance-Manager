package soft.divan.financemanager.sync.data.source.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
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

class SyncLocalSourceImplTest {

    private val dataStore = mockk<DataStore<Preferences>>()
    private val localSource = SyncLocalSourceImpl(dataStore)

    @Test
    fun `observeLastSyncTime returns stored value`() = runTest {
        every { dataStore.data } returns flowOf(preferencesOf(KEY_LAST_SYNC_TIME to 42L))

        assertThat(localSource.observeLastSyncTime().first()).isEqualTo(42L)
    }

    @Test
    fun `observeLastSyncTime returns null when nothing stored`() = runTest {
        every { dataStore.data } returns flowOf(emptyPreferences())

        assertThat(localSource.observeLastSyncTime().first()).isNull()
    }

    @Test
    fun `setLastSyncTime stores value under last sync key`() = runTest {
        val transform = slot<suspend (Preferences) -> Preferences>()
        coEvery { dataStore.updateData(capture(transform)) } coAnswers {
            transform.captured(emptyPreferences())
        }

        localSource.setLastSyncTime(777L)

        val stored = transform.captured(emptyPreferences())
        assertThat(stored[KEY_LAST_SYNC_TIME]).isEqualTo(777L)
    }

    @Test
    fun `observeSyncIntervalHours returns stored value`() = runTest {
        every { dataStore.data } returns flowOf(preferencesOf(KEY_SYNC_INTERVAL_HOURS to 6))

        assertThat(localSource.observeSyncIntervalHours().first()).isEqualTo(6)
    }

    @Test
    fun `observeSyncIntervalHours returns null when nothing stored`() = runTest {
        every { dataStore.data } returns flowOf(emptyPreferences())

        assertThat(localSource.observeSyncIntervalHours().first()).isNull()
    }

    @Test
    fun `setSyncIntervalHours stores value under interval key`() = runTest {
        val transform = slot<suspend (Preferences) -> Preferences>()
        coEvery { dataStore.updateData(capture(transform)) } coAnswers {
            transform.captured(emptyPreferences())
        }

        localSource.setSyncIntervalHours(12)

        val stored = transform.captured(emptyPreferences())
        assertThat(stored[KEY_SYNC_INTERVAL_HOURS]).isEqualTo(12)
    }
}
