package soft.divan.financemanager.feature.design_app.design_app_impl.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.design_app.design_app_impl.data.source.DesignAppLocalSource
import soft.divan.financemanager.feature.design_app.design_app_impl.data.source.impl.DesignAppLocalSourceImpl
import javax.inject.Singleton

val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore("user_preferences")

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

