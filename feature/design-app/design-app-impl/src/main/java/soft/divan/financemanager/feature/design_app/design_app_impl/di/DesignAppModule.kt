package soft.divan.financemanager.feature.design_app.design_app_impl.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.design_app.design_app_api.DesignAppFeatureApi
import soft.divan.financemanager.feature.design_app.design_app_impl.data.DesignAppRepositoryImpl
import soft.divan.financemanager.feature.design_app.design_app_impl.data.source.DesignAppLocalSource
import soft.divan.financemanager.feature.design_app.design_app_impl.data.source.impl.DesignAppLocalSourceImpl
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.repositiry.DesignAppRepository
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase.GetAccentColorUseCase
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase.GetCustomAccentColorUseCase
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase.GetThemeModeUseCase
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase.SetAccentColorUseCase
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase.SetCustomAccentColorUseCase
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase.SetThemeModeUseCase
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase.impl.GetAccentColorUseCaseImpl
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase.impl.GetCustomAccentColorUseCaseImpl
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase.impl.GetThemeModeUseCaseImpl
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase.impl.SetAccentColorUseCaseImpl
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase.impl.SetCustomAccentColorUseCaseImpl
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.usecase.impl.SetThemeModeUseCaseImpl
import soft.divan.financemanager.feature.design_app.design_app_impl.navigation.DesignAppFeatureImpl
import javax.inject.Qualifier
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
interface DesignAppModule {

    @Binds
    fun bindDesignAppRouter(impl: DesignAppFeatureImpl): DesignAppFeatureApi

    @Binds
    @Singleton
    fun bindThemeRepository(impl: DesignAppRepositoryImpl): DesignAppRepository

}

val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore("user_preferences")

@Module
@InstallIn(SingletonComponent::class)
object ThemeDataStoreModule {

    @Provides
    @Singleton
    @ThemeDataStore
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.themeDataStore
    }
}


@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ThemeDataStore

@Module
@InstallIn(SingletonComponent::class)
object ThemeLocalModule {

    @Provides
    @Singleton
    fun provideThemePreferences(@ThemeDataStore dataStore: DataStore<Preferences>): DesignAppLocalSource {
        return DesignAppLocalSourceImpl(dataStore)
    }

}

@Module
@InstallIn(SingletonComponent::class)
interface ThemeUseCaseModule {

    @Binds
    fun bindGetThemeModeUseCase(impl: GetThemeModeUseCaseImpl): GetThemeModeUseCase

    @Binds
    fun bindSetThemeModeUseCase(impl: SetThemeModeUseCaseImpl): SetThemeModeUseCase

    @Binds
    fun bindGetAccentColorUseCase(impl: GetAccentColorUseCaseImpl): GetAccentColorUseCase

    @Binds
    fun bindSetAccentColorUseCase(impl: SetAccentColorUseCaseImpl): SetAccentColorUseCase

    @Binds
    fun bindGetCustomAccentColorUseCase(impl: GetCustomAccentColorUseCaseImpl): GetCustomAccentColorUseCase

    @Binds
    fun bindSetCustomAccentColorUseCase(impl: SetCustomAccentColorUseCaseImpl): SetCustomAccentColorUseCase

}
