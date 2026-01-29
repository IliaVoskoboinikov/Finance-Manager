package soft.divan.financemanager.sync.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.sync.data.source.SyncLocalSource
import soft.divan.financemanager.sync.data.source.impl.SyncLocalSourceImpl
import javax.inject.Qualifier
import javax.inject.Singleton

val Context.syncDataStore: DataStore<Preferences> by preferencesDataStore("sync_preferences")

@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Provides
    @Singleton
    @SyncDataStore
    fun provideSyncDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.syncDataStore
    }

    @Provides
    @Singleton
    fun providePreferences(@SyncDataStore dataStore: DataStore<Preferences>): SyncLocalSource {
        return SyncLocalSourceImpl(dataStore)
    }
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SyncDataStore