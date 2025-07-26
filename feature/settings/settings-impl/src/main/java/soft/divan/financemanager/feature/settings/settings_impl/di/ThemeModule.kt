package soft.divan.financemanager.feature.settings.settings_impl.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.settings.settings_impl.data.ThemeRepositoryImpl
import soft.divan.financemanager.feature.settings.settings_impl.domain.repositiry.ThemeRepository
import soft.divan.financemanager.feature.settings.settings_impl.domain.usecase.GetThemeModeUseCase
import soft.divan.financemanager.feature.settings.settings_impl.domain.usecase.SetThemeModeUseCase
import soft.divan.financemanager.feature.settings.settings_impl.domain.usecase.impl.GetThemeModeUseCaseImpl
import soft.divan.financemanager.feature.settings.settings_impl.domain.usecase.impl.SetThemeModeUseCaseImpl
import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ThemeDataStore

@Module
@InstallIn(SingletonComponent::class)
object ThemeModule {

    private val Context.dataStore by preferencesDataStore("user_preferences")

    @Provides
    @ThemeDataStore
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    fun provideThemePreferences(@ThemeDataStore dataStore: DataStore<Preferences>): ThemeRepository {
        return ThemeRepositoryImpl(dataStore)
    }

    @Provides
    fun provideGetThemeUseCase(impl: GetThemeModeUseCaseImpl): GetThemeModeUseCase {
        return impl
    }

    @Provides
    fun provideSetThemeUseCase(impl: SetThemeModeUseCaseImpl): SetThemeModeUseCase {
        return impl
    }

}
