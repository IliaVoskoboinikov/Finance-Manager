package soft.divan.financemanager.feature.languages.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.languages.api.LanguagesFeatureApi
import soft.divan.financemanager.feature.languages.impl.data.repository.LanguageRepositoryImpl
import soft.divan.financemanager.feature.languages.impl.domain.locale.AppLocaleManager
import soft.divan.financemanager.feature.languages.impl.domain.repository.LanguageRepository
import soft.divan.financemanager.feature.languages.impl.domain.usecase.ObserveLanguagesUseCase
import soft.divan.financemanager.feature.languages.impl.domain.usecase.SetLanguageUseCase
import soft.divan.financemanager.feature.languages.impl.domain.usecase.impl.ObserveLanguageUseCaseImpl
import soft.divan.financemanager.feature.languages.impl.domain.usecase.impl.SetLanguageUseCaseImpl
import soft.divan.financemanager.feature.languages.impl.navigation.LanguagesFeatureImpl
import soft.divan.financemanager.feature.languages.impl.system.AppLocaleManagerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface LanguagesBinderModule {

    @Binds
    fun bindLanguagesRouter(impl: LanguagesFeatureImpl): LanguagesFeatureApi

    @Binds
    @Singleton
    fun bindLanguageRepository(impl: LanguageRepositoryImpl): LanguageRepository

    @Binds
    fun bindObserveLanguageEnabledUseCase(impl: ObserveLanguageUseCaseImpl): ObserveLanguagesUseCase

    @Binds
    fun bindSetLanguageEnabledUseCase(impl: SetLanguageUseCaseImpl): SetLanguageUseCase

    @Binds
    fun bindAppLocaleManager(impl: AppLocaleManagerImpl): AppLocaleManager
}
