package soft.divan.financemanager.feature.security.impl.data.sourse.impl

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import soft.divan.financemanager.feature.security.impl.data.sourse.SecurityLocalDataSource
import javax.inject.Inject

private const val PREFS_FILE_NAME = "secure_prefs"
private const val USER_PIN_KEY = "user_pin"

class SecurityLocalDataSourceImpl @Inject constructor(
    context: Context
) : SecurityLocalDataSource {

    private val sharedPrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun savePin(pin: String) {
        sharedPrefs.edit { putString(USER_PIN_KEY, pin) }
    }

    override fun getPin(): String? {
        return sharedPrefs.getString(USER_PIN_KEY, null)
    }

    override fun isPinSet(): Boolean = getPin() != null

    override fun deletePin() {
        sharedPrefs.edit { remove(USER_PIN_KEY) }
    }
}
