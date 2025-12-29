package soft.divan.financemanager.sync.data.sourсe.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import soft.divan.financemanager.sync.data.sourсe.SyncLocalSource
import javax.inject.Inject

val KEY_LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
val KEY_SYNC_INTERVAL_HOURS = intPreferencesKey("sync_interval_hours")

class SyncLocalSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SyncLocalSource {

    override fun observeLastSyncTime(): Flow<Long?> {
        return dataStore.data.map { it[KEY_LAST_SYNC_TIME] }
    }

    override suspend fun setLastSyncTime(timeMillis: Long) {
        dataStore.edit { it[KEY_LAST_SYNC_TIME] = timeMillis }
    }

    override fun observeSyncIntervalHours(): Flow<Int?> {
        return dataStore.data.map { it[KEY_SYNC_INTERVAL_HOURS] }
    }

    override suspend fun setSyncIntervalHours(hours: Int) {
        dataStore.edit { it[KEY_SYNC_INTERVAL_HOURS] = hours }
    }

}
