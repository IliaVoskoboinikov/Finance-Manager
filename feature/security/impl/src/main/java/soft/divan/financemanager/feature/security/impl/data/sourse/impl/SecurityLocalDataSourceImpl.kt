package soft.divan.financemanager.feature.security.impl.data.sourse.impl

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import soft.divan.financemanager.core.security.CryptoManager
import soft.divan.financemanager.feature.security.impl.data.sourse.SecurityLocalDataSource
import javax.inject.Inject

private const val PREFS_FILE_NAME = "security_prefs"
private const val USER_PIN_KEY = "user_pin"
private const val PIN_ALIAS = "pin_alias"

/**
 * Хранит PIN (уже хешированный в репозитории) зашифрованным через общий [CryptoManager]
 * (AES/GCM, AndroidKeyStore) в обычных SharedPreferences.
 *
 * Раньше использовался androidx.security.crypto (EncryptedSharedPreferences, deprecated) —
 * теперь крипто-подсистема единая для токенов и PIN.
 */
class SecurityLocalDataSourceImpl @Inject constructor(
    @ApplicationContext context: Context,
    private val cryptoManager: CryptoManager
) : SecurityLocalDataSource {

    private val sharedPrefs: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
    }

    override fun savePin(pin: String) {
        val encrypted = cryptoManager.encrypt(pin, PIN_ALIAS)
        sharedPrefs.edit { putString(USER_PIN_KEY, encrypted) }
    }

    override fun getPin(): String? {
        val encrypted = sharedPrefs.getString(USER_PIN_KEY, null) ?: return null
        return runCatching { cryptoManager.decrypt(encrypted, PIN_ALIAS) }.getOrNull()
    }

    override fun isPinSet(): Boolean = sharedPrefs.contains(USER_PIN_KEY)

    override fun deletePin() {
        sharedPrefs.edit { remove(USER_PIN_KEY) }
    }
}
