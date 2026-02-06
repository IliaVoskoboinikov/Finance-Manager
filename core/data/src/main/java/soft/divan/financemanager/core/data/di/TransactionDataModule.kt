package soft.divan.financemanager.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.core.data.repository.TransactionRepositoryImpl
import soft.divan.financemanager.core.data.source.TransactionLocalDataSource
import soft.divan.financemanager.core.data.source.TransactionRemoteDataSource
import soft.divan.financemanager.core.data.source.impl.TransactionLocalDataSourceImpl
import soft.divan.financemanager.core.data.source.impl.TransactionRemoteDataSourceImpl
import soft.divan.financemanager.core.domain.repository.TransactionRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface TransactionDataModule {

    @Binds
    @Singleton
    fun bindTransactionRemoteDataSource(
        impl: TransactionRemoteDataSourceImpl
    ): TransactionRemoteDataSource

    @Binds
    @Singleton
    fun bindTransactionLocalDataSource(
        impl: TransactionLocalDataSourceImpl
    ): TransactionLocalDataSource

    @Binds
    @Singleton
    fun bindTransactionRepository(
        impl: TransactionRepositoryImpl
    ): TransactionRepository
}
// Revue me>>
