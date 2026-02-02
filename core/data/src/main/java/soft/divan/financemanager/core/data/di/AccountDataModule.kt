package soft.divan.financemanager.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.core.data.repository.AccountRepositoryImpl
import soft.divan.financemanager.core.data.source.AccountLocalDataSource
import soft.divan.financemanager.core.data.source.AccountRemoteDataSource
import soft.divan.financemanager.core.data.source.impl.AccountLocalDataSourceImpl
import soft.divan.financemanager.core.data.source.impl.AccountRemoteDataSourceImpl
import soft.divan.financemanager.core.domain.repository.AccountRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AccountDataModule {

    @Binds
    @Singleton
    fun bindAccountRemoteDataSource(
        impl: AccountRemoteDataSourceImpl
    ): AccountRemoteDataSource

    @Binds
    @Singleton
    fun bindAccountLocalDataSource(
        impl: AccountLocalDataSourceImpl
    ): AccountLocalDataSource

    @Binds
    @Singleton
    fun bindAccountRepository(
        impl: AccountRepositoryImpl
    ): AccountRepository
}
