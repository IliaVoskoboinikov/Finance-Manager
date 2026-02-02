package soft.divan.financemanager.sync.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.sync.data.source.SyncLocalSource
import soft.divan.financemanager.sync.data.source.impl.SyncLocalSourceImpl
import javax.inject.Singleton

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
