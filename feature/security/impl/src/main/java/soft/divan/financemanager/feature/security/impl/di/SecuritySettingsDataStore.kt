package soft.divan.financemanager.feature.security.impl.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import javax.inject.Qualifier

/** DataStore настроек безопасности, не требующих шифрования (например, защита экрана). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SecuritySettingsDataStore

/** Файл настроек безопасности. */
val Context.securitySettingsDataStore: DataStore<Preferences> by preferencesDataStore(
    "security_settings"
)
