package soft.divan.financemanager.feature.security.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.security.impl.domain.usecase.ChangeProtectionLevelUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.ObserveProtectionLevelUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.ObserveSecureScreenUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.ObserveSecuritySettingsUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.RecoverLocalDataUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.SetSecureScreenUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.impl.ChangeProtectionLevelUseCaseImpl
import soft.divan.financemanager.feature.security.impl.domain.usecase.impl.ObserveProtectionLevelUseCaseImpl
import soft.divan.financemanager.feature.security.impl.domain.usecase.impl.ObserveSecureScreenUseCaseImpl
import soft.divan.financemanager.feature.security.impl.domain.usecase.impl.ObserveSecuritySettingsUseCaseImpl
import soft.divan.financemanager.feature.security.impl.domain.usecase.impl.RecoverLocalDataUseCaseImpl
import soft.divan.financemanager.feature.security.impl.domain.usecase.impl.SetSecureScreenUseCaseImpl

/** Сценарии защиты данных: уровень защиты, восстановление после потери ключа, защита экрана. */
@Module
@InstallIn(SingletonComponent::class)
interface DataProtectionUseCaseModule {

    @Binds
    fun bindRecoverLocalDataUseCase(impl: RecoverLocalDataUseCaseImpl): RecoverLocalDataUseCase

    @Binds
    fun bindChangeProtectionLevelUseCase(
        impl: ChangeProtectionLevelUseCaseImpl
    ): ChangeProtectionLevelUseCase

    @Binds
    fun bindObserveProtectionLevelUseCase(
        impl: ObserveProtectionLevelUseCaseImpl
    ): ObserveProtectionLevelUseCase

    @Binds
    fun bindObserveSecuritySettingsUseCase(
        impl: ObserveSecuritySettingsUseCaseImpl
    ): ObserveSecuritySettingsUseCase

    @Binds
    fun bindObserveSecureScreenUseCase(impl: ObserveSecureScreenUseCaseImpl): ObserveSecureScreenUseCase

    @Binds
    fun bindSetSecureScreenUseCase(impl: SetSecureScreenUseCaseImpl): SetSecureScreenUseCase
}
