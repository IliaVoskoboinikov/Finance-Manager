package soft.divan.financemanager.feature.designapp.impl.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore("user_preferences")
// Revue me>>
