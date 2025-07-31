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


@Module
@InstallIn(SingletonComponent::class)
abstract class DesignAppModule {

    @Binds
    abstract fun bindDesignAppRouter(impl: DesignAppFeatureImpl): DesignAppFeatureApi

    @Binds
    abstract fun bindThemeRepository(impl: DesignAppRepositoryImpl): DesignAppRepository

}


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
    fun provideThemePreferences(@ThemeDataStore dataStore: DataStore<Preferences>): DesignAppLocalSource {
        return DesignAppLocalSourceImpl(dataStore)
    }

    @Provides
    fun provideGetThemeUseCase(impl: GetThemeModeUseCaseImpl): GetThemeModeUseCase {
        return impl
    }

    @Provides
    fun provideSetThemeUseCase(impl: SetThemeModeUseCaseImpl): SetThemeModeUseCase {
        return impl
    }

    @Provides
    fun provideGetAccentColorUseCaseImplUseCase(impl: GetAccentColorUseCaseImpl): GetAccentColorUseCase {
        return impl
    }

    @Provides
    fun provideSetAccentColorUseCase(impl: SetAccentColorUseCaseImpl): SetAccentColorUseCase {
        return impl
    }

    @Provides
    fun provideGetCustomAccentColorUseCase(impl: GetCustomAccentColorUseCaseImpl): GetCustomAccentColorUseCase {
        return impl
    }

    @Provides
    fun provideSetCustomAccentColorUseCase(impl: SetCustomAccentColorUseCaseImpl): SetCustomAccentColorUseCase {
        return impl
    }

}