package soft.divan.financemanager.feature.languages.impl.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.languagesDataStore: DataStore<Preferences> by preferencesDataStore(
    "language_preferences"
)
// Revue me>>
