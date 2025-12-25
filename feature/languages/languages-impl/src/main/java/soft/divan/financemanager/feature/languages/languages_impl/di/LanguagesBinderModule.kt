package soft.divan.financemanager.feature.languages.languages_impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.languages.languages_api.LanguagesFeatureApi
import soft.divan.financemanager.feature.languages.languages_impl.data.repository.LanguageRepositoryImpl
import soft.divan.financemanager.feature.languages.languages_impl.domain.locale.AppLocaleManager
import soft.divan.financemanager.feature.languages.languages_impl.domain.repository.LanguageRepository
import soft.divan.financemanager.feature.languages.languages_impl.domain.usecase.ObserveLanguagesUseCase
import soft.divan.financemanager.feature.languages.languages_impl.domain.usecase.SetLanguageUseCase
import soft.divan.financemanager.feature.languages.languages_impl.domain.usecase.impl.ObserveLanguageUseCaseImpl
import soft.divan.financemanager.feature.languages.languages_impl.domain.usecase.impl.SetLanguageUseCaseImpl
import soft.divan.financemanager.feature.languages.languages_impl.navigation.LanguagesFeatureImpl
import soft.divan.financemanager.feature.languages.languages_impl.system.AppLocaleManagerImpl
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
