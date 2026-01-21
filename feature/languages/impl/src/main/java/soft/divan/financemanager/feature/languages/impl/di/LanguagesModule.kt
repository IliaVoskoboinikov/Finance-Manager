package soft.divan.financemanager.feature.languages.impl.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.languages.impl.data.sourсe.LanguagesLocalSource
import soft.divan.financemanager.feature.languages.impl.data.sourсe.impl.LanguageLocalSourceImpl
import javax.inject.Singleton

val Context.languagesDataStore: DataStore<Preferences> by preferencesDataStore("language_preferences")

@Module
@InstallIn(SingletonComponent::class)
object LanguagesModule {

    @Provides
    @Singleton
    @LanguagesDataStore
    fun provideLanguagesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.languagesDataStore
    }

    @Provides
    @Singleton
    fun provideLanguagesPreferences(@LanguagesDataStore dataStore: DataStore<Preferences>): LanguagesLocalSource {
        return LanguageLocalSourceImpl(dataStore)
    }
}