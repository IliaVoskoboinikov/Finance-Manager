package soft.divan.financemanager.sync.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.syncDataStore: DataStore<Preferences> by preferencesDataStore("sync_preferences")
