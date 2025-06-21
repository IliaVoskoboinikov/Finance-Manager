package soft.divan.financemanager.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.data.repository.AccountRepositoryImpl
import soft.divan.financemanager.data.source.AccountRemoteDataSource
import soft.divan.financemanager.data.source.AccountRemoteDataSourceImpl
import soft.divan.financemanager.domain.repository.AccountRepository

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    fun provideAccountRemoteDataSource(
        impl: AccountRemoteDataSourceImpl
    ): AccountRemoteDataSource {
        return impl
    }

    @Provides
    fun provideAccountRepository(
        impl: AccountRepositoryImpl
    ): AccountRepository {
        return impl
    }


}