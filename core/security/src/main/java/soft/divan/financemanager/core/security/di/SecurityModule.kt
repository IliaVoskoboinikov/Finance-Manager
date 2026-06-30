package soft.divan.financemanager.core.security.di

import android.security.keystore.KeyProperties
import android.util.Log
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    private const val TAG = "SecurityModule"
    const val AES_TRANSFORMATION = "AES/GCM/NoPadding"

    @Provides
    @Singleton
    fun provideKeyStore(): KeyStore {
        return try {
            KeyStore.getInstance("AndroidKeyStore").apply {
                load(null)
            }
        } catch (e: GeneralSecurityException) {
            Log.e(TAG, "Security error loading AndroidKeyStore", e)
            throw e
        } catch (e: IOException) {
            Log.e(TAG, "IO error loading AndroidKeyStore", e)
            throw e
        }
    }

    @Provides
    @Singleton
    fun provideKeyGenerator(): KeyGenerator {
        return try {
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        } catch (e: GeneralSecurityException) {
            Log.e(TAG, "Failed to get AES KeyGenerator for AndroidKeyStore", e)
            throw e
        }
    }

    @Provides
    @Named("AES_CIPHER")
    fun provideAesCipher(): Cipher {
        return try {
            Cipher.getInstance(AES_TRANSFORMATION)
        } catch (e: GeneralSecurityException) {
            Log.e(TAG, "Failed to get Cipher for $AES_TRANSFORMATION", e)
            throw e
        }
    }
}
