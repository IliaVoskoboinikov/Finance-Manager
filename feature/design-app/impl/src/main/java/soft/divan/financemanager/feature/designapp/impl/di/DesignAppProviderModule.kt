package soft.divan.financemanager.feature.designapp.impl.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.designapp.impl.data.source.DesignAppLocalSource
import soft.divan.financemanager.feature.designapp.impl.data.source.impl.DesignAppLocalSourceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DesignAppProviderModule {

    @Provides
    @Singleton
    @ThemeDataStore
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.themeDataStore
    }

    @Provides
    @Singleton
    fun provideThemePreferences(@ThemeDataStore dataStore: DataStore<Preferences>): DesignAppLocalSource {
        return DesignAppLocalSourceImpl(dataStore)
    }
}
