package soft.divan.financemanager.category.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.category.data.repository.CategoryRepositoryImpl
import soft.divan.financemanager.category.data.source.CategoryRemoteDataSource
import soft.divan.financemanager.category.data.source.CategoryRemoteDataSourceImpl
import soft.divan.financemanager.category.domain.repository.CategoryRepository
import soft.divan.financemanager.category.domain.usecase.GetCategoriesUseCase
import soft.divan.financemanager.category.domain.usecase.SearchCategoryUseCase
import soft.divan.financemanager.category.domain.usecase.impl.GetCategoriesUseCaseImpl
import soft.divan.financemanager.category.domain.usecase.impl.SearchCategoryUseCaseImpl

@Module
@InstallIn(SingletonComponent::class)
abstract class CategoryModule {

    @Binds
    abstract fun bindGetCategoriesUseCase(
        impl: GetCategoriesUseCaseImpl
    ): GetCategoriesUseCase

    @Binds
    abstract fun bindSearchCategoryUseCase(
        impl: SearchCategoryUseCaseImpl
    ): SearchCategoryUseCase

    @Binds
    abstract fun bindCategoryRemoteDataSource(
        impl: CategoryRemoteDataSourceImpl
    ): CategoryRemoteDataSource

    @Binds
    abstract fun bindCategoryRepository(
        impl: CategoryRepositoryImpl
    ): CategoryRepository

}