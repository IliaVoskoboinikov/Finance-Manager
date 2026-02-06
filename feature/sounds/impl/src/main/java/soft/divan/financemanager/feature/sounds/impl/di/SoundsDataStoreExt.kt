package soft.divan.financemanager.feature.sounds.impl.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.soundDataStore: DataStore<Preferences> by preferencesDataStore("sound_preferences")
// Revue me>>
