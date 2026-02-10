package soft.divan.financemanager.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.core.data.repository.CategoryRepositoryImpl
import soft.divan.financemanager.core.data.source.CategoryLocalDataSource
import soft.divan.financemanager.core.data.source.CategoryRemoteDataSource
import soft.divan.financemanager.core.data.source.impl.CategoryLocalDataSourceImpl
import soft.divan.financemanager.core.data.source.impl.CategoryRemoteDataSourceImpl
import soft.divan.financemanager.core.domain.repository.CategoryRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface CategoryDataModule {

    @Binds
    @Singleton
    fun bindCategoryRemoteDataSource(
        impl: CategoryRemoteDataSourceImpl
    ): CategoryRemoteDataSource

    @Binds
    @Singleton
    fun bindCategoryLocalDataSource(
        impl: CategoryLocalDataSourceImpl
    ): CategoryLocalDataSource

    @Binds
    @Singleton
    fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository
}
// Revue me>>
