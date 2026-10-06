package soft.divan.financemanager.core.data.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import soft.divan.common.di.IoDispatcher
import soft.divan.financemanager.core.data.repository.DataProtectionRepositoryImpl
import soft.divan.financemanager.core.data.repository.LocalDataAccessRepositoryImpl
import soft.divan.financemanager.core.data.vault.BiometricVault
import soft.divan.financemanager.core.data.vault.CryptoShredCleanupManager
import soft.divan.financemanager.core.data.vault.LocalDataBiometrics
import soft.divan.financemanager.core.database.holder.DatabaseFactory
import soft.divan.financemanager.core.database.holder.DatabaseFiles
import soft.divan.financemanager.core.database.holder.DatabaseHolder
import soft.divan.financemanager.core.database.holder.RoomDatabaseHolder
import soft.divan.financemanager.core.database.util.DatabaseCleanupManager
import soft.divan.financemanager.core.domain.repository.DataProtectionRepository
import soft.divan.financemanager.core.domain.repository.LocalDataAccessRepository
import soft.divan.financemanager.core.domain.usecase.InitializeLocalDataUseCase
import soft.divan.financemanager.core.domain.usecase.LockLocalDataUseCase
import soft.divan.financemanager.core.domain.usecase.ObserveLocalDataStateUseCase
import soft.divan.financemanager.core.domain.usecase.impl.InitializeLocalDataUseCaseImpl
import soft.divan.financemanager.core.domain.usecase.impl.LockLocalDataUseCaseImpl
import soft.divan.financemanager.core.domain.usecase.impl.ObserveLocalDataStateUseCaseImpl
import javax.inject.Singleton

/** Шифрование локальных данных: холдер базы, хранилище ключей и доменные входы в них. */
@Module
@InstallIn(SingletonComponent::class)
interface LocalDataModule {

    @Binds
    @Singleton
    fun bindLocalDataAccessRepository(impl: LocalDataAccessRepositoryImpl): LocalDataAccessRepository

    @Binds
    @Singleton
    fun bindDataProtectionRepository(impl: DataProtectionRepositoryImpl): DataProtectionRepository

    @Binds
    @Singleton
    fun bindLocalDataBiometrics(impl: BiometricVault): LocalDataBiometrics

    @Binds
    @Singleton
    fun bindDatabaseCleanupManager(impl: CryptoShredCleanupManager): DatabaseCleanupManager

    @Binds
    fun bindObserveLocalDataStateUseCase(
        impl: ObserveLocalDataStateUseCaseImpl
    ): ObserveLocalDataStateUseCase

    @Binds
    fun bindInitializeLocalDataUseCase(impl: InitializeLocalDataUseCaseImpl): InitializeLocalDataUseCase

    @Binds
    fun bindLockLocalDataUseCase(impl: LockLocalDataUseCaseImpl): LockLocalDataUseCase

    companion object {

        /**
         * Холдер создаётся здесь, а не в `:core:database`: ему нужен IO-диспетчер из
         * `:core:common`, а зависимость `:core:database → :core:common` углубила бы граф модулей
         * сверх порога `assertModuleGraph`.
         */
        @Provides
        @Singleton
        fun provideDatabaseHolder(
            factory: DatabaseFactory,
            files: DatabaseFiles,
            @IoDispatcher dispatcher: CoroutineDispatcher
        ): DatabaseHolder = RoomDatabaseHolder(factory, files, dispatcher)
    }
}
