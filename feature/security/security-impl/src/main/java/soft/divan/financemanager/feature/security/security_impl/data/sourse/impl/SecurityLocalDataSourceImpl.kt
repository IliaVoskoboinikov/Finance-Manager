package soft.divan.financemanager.feature.security.security_impl.data.sourse.impl

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import soft.divan.financemanager.feature.security.security_impl.data.sourse.SecurityLocalDataSource
import javax.inject.Inject

class SecurityLocalDataSourceImpl @Inject constructor(
    context: Context
) : SecurityLocalDataSource {
    private val sharedPrefs: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun savePin(pin: String) {
        sharedPrefs.edit { putString("user_pin", pin) }
    }

    override fun getPin(): String? {
        return sharedPrefs.getString("user_pin", null)
    }

    override fun isPinSet(): Boolean = getPin() != null
}