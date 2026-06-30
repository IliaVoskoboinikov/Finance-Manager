package soft.divan.financemanager.core.auth.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore("session_preferences")

val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore("token_preferences")
