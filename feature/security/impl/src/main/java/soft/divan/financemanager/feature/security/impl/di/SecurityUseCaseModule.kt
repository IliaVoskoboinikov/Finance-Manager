package soft.divan.financemanager.feature.security.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.security.impl.domain.usecase.ChangePinUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.DeletePinUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.ForgetPinUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.GetPinLockStatusUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.ObservePinSetUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.SavePinUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.UnlockWithPinUseCase
import soft.divan.financemanager.feature.security.impl.domain.usecase.impl.ChangePinUseCaseImpl
import soft.divan.financemanager.feature.security.impl.domain.usecase.impl.DeletePinUseCaseImpl
import soft.divan.financemanager.feature.security.impl.domain.usecase.impl.ForgetPinUseCaseImpl
import soft.divan.financemanager.feature.security.impl.domain.usecase.impl.GetPinLockStatusUseCaseImpl
import soft.divan.financemanager.feature.security.impl.domain.usecase.impl.ObservePinSetUseCaseImpl
import soft.divan.financemanager.feature.security.impl.domain.usecase.impl.SavePinUseCaseImpl
import soft.divan.financemanager.feature.security.impl.domain.usecase.impl.UnlockWithPinUseCaseImpl

/** Сценарии PIN: создать, сменить, удалить, открыть замок, «забыл PIN». */
@Module
@InstallIn(SingletonComponent::class)
interface SecurityUseCaseModule {

    @Binds
    fun bindSavePinUseCase(impl: SavePinUseCaseImpl): SavePinUseCase

    @Binds
    fun bindObservePinSetUseCase(impl: ObservePinSetUseCaseImpl): ObservePinSetUseCase

    @Binds
    fun bindDeletePinUseCase(impl: DeletePinUseCaseImpl): DeletePinUseCase

    @Binds
    fun bindChangePinUseCase(impl: ChangePinUseCaseImpl): ChangePinUseCase

    @Binds
    fun bindUnlockWithPinUseCase(impl: UnlockWithPinUseCaseImpl): UnlockWithPinUseCase

    @Binds
    fun bindGetPinLockStatusUseCase(impl: GetPinLockStatusUseCaseImpl): GetPinLockStatusUseCase

    @Binds
    fun bindForgetPinUseCase(impl: ForgetPinUseCaseImpl): ForgetPinUseCase
}
