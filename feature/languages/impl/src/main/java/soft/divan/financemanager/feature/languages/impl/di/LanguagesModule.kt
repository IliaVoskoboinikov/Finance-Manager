package soft.divan.financemanager.feature.languages.impl.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.languages.impl.data.source.LanguagesLocalSource
import soft.divan.financemanager.feature.languages.impl.data.source.impl.LanguageLocalSourceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LanguagesModule {

    @Provides
    @Singleton
    @LanguagesDataStore
    fun provideLanguagesDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.languagesDataStore

    @Provides
    @Singleton
    fun provideLanguagesPreferences(
        @LanguagesDataStore dataStore: DataStore<Preferences>
    ): LanguagesLocalSource =
        LanguageLocalSourceImpl(dataStore)
}
