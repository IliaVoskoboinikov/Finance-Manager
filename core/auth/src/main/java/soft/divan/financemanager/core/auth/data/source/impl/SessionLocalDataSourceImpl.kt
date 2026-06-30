package soft.divan.financemanager.core.auth.data.source.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import soft.divan.financemanager.core.auth.data.source.SessionLocalDataSource
import soft.divan.financemanager.core.auth.domain.model.AuthStatus
import soft.divan.financemanager.core.domain.utli.toEnumOrNull
import javax.inject.Inject

private val AUTH_STATUS_KEY = stringPreferencesKey("auth_status")

class SessionLocalDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SessionLocalDataSource {

    override fun getAuthStatus(): Flow<AuthStatus> {
        return dataStore.data.map { preferences ->
            preferences[AUTH_STATUS_KEY]?.toEnumOrNull<AuthStatus>() ?: AuthStatus.UNAUTHORIZED
        }
    }

    override suspend fun setAuthStatus(status: AuthStatus) {
        dataStore.edit { preferences ->
            preferences[AUTH_STATUS_KEY] = status.name
        }
    }

    override suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences[AUTH_STATUS_KEY] = AuthStatus.UNAUTHORIZED.name
        }
    }
}
