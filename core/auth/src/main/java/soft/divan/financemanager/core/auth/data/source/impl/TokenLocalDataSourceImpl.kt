package soft.divan.financemanager.core.auth.data.source.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import soft.divan.financemanager.core.auth.data.source.TokenLocalDataSource
import soft.divan.financemanager.core.security.CryptoManager
import javax.inject.Inject

private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
private const val AUTH_ALIAS_KEY = "auth_token_key"

class TokenLocalDataSourceImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val cryptoManager: CryptoManager
) : TokenLocalDataSource {

    override fun getAccessToken(): Flow<String?> {
        return dataStore.data
            .map { preferences ->
                preferences[ACCESS_TOKEN_KEY]?.let { encrypted ->
                    try {
                        cryptoManager.decrypt(encrypted, AUTH_ALIAS_KEY)
                    } catch (_: Exception) {
                        null
                    }
                }
            }
    }

    override suspend fun updateAccessToken(token: String?) {
        dataStore.edit { preferences ->
            if (token != null) {
                val encrypted = cryptoManager.encrypt(token, AUTH_ALIAS_KEY)
                preferences[ACCESS_TOKEN_KEY] = encrypted
            } else {
                preferences.remove(ACCESS_TOKEN_KEY)
            }
        }
    }

    override fun getRefreshToken(): Flow<String?> {
        return dataStore.data
            .map { preferences ->
                preferences[REFRESH_TOKEN_KEY]?.let { encrypted ->
                    try {
                        cryptoManager.decrypt(encrypted, AUTH_ALIAS_KEY)
                    } catch (_: Exception) {
                        null
                    }
                }
            }
    }

    override suspend fun updateRefreshToken(token: String?) {
        dataStore.edit { preferences ->
            if (token != null) {
                val encrypted = cryptoManager.encrypt(token, AUTH_ALIAS_KEY)
                preferences[REFRESH_TOKEN_KEY] = encrypted
            } else {
                preferences.remove(REFRESH_TOKEN_KEY)
            }
        }
    }

    override suspend fun clearTokens() {
        dataStore.edit { preferences ->
            preferences.remove(ACCESS_TOKEN_KEY)
            preferences.remove(REFRESH_TOKEN_KEY)
        }
    }
}
