package soft.divan.financemanager.core.security.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.core.security.keyset.KeysetStore
import soft.divan.financemanager.core.security.keyset.impl.DataStoreKeysetStore
import soft.divan.financemanager.core.security.keystore.KeystoreKeys
import soft.divan.financemanager.core.security.keystore.impl.AndroidKeystoreKeys
import java.io.File
import javax.inject.Singleton

/** Ключи шифрования базы: Keystore и хранилище завёрнутых ключей. */
@Module
@InstallIn(SingletonComponent::class)
interface EncryptionModule {

    @Binds
    @Singleton
    fun bindKeystoreKeys(impl: AndroidKeystoreKeys): KeystoreKeys

    @Binds
    @Singleton
    fun bindKeysetStore(impl: DataStoreKeysetStore): KeysetStore

    companion object {

        /** Относительный путь внутри `noBackupFilesDir`; расширение требует Preferences DataStore. */
        const val KEYSET_FILE = "security/keyset.preferences_pb"

        /**
         * Набор ключей лежит в `noBackupFilesDir`: система не включает этот каталог ни в облачный
         * бэкап, ни в перенос на новое устройство. Завёрнутые ключи там бесполезны (ключи Keystore
         * не переезжают), а открытый ключ уровня «без защиты» попасть туда не должен.
         *
         * Испорченный файл превращается в пустой набор, а не в исключение на каждом чтении: иначе
         * его нельзя было бы и перезаписать при восстановлении. Пустой набор рядом с существующей
         * базой хранилище ключей трактует как потерю ключа, а не как первый запуск.
         */
        @Provides
        @Singleton
        @KeysetDataStore
        fun provideKeysetDataStore(
            @ApplicationContext context: Context
        ): DataStore<Preferences> = PreferenceDataStoreFactory.create(
            corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
            produceFile = { File(context.noBackupFilesDir, KEYSET_FILE) }
        )
    }
}
