package soft.divan.financemanager.feature.design_app.design_app_impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.design_app.design_app_api.DesignAppFeatureApi
import soft.divan.financemanager.feature.design_app.design_app_impl.data.repository.DesignAppRepositoryImpl
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
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
interface DesignAppBinderModule {

    @Binds
    fun bindDesignAppRouter(impl: DesignAppFeatureImpl): DesignAppFeatureApi

    @Binds
    @Singleton
    fun bindThemeRepository(impl: DesignAppRepositoryImpl): DesignAppRepository

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
