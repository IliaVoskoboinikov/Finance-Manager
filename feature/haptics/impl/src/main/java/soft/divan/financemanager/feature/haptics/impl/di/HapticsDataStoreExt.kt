package soft.divan.financemanager.feature.haptics.impl.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.hapticsDataStore: DataStore<Preferences> by preferencesDataStore("haptics_preferences")
// Revue me>>
