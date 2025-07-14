package soft.divan.financemanager.feature.category.category_impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.category.category_api.CategoryFeatureApi
import soft.divan.financemanager.feature.category.category_impl.data.repository.CategoryRepositoryImpl
import soft.divan.financemanager.feature.category.category_impl.data.source.CategoryRemoteDataSource
import soft.divan.financemanager.feature.category.category_impl.data.source.CategoryRemoteDataSourceImpl
import soft.divan.financemanager.feature.category.category_impl.domain.repository.CategoryRepository
import soft.divan.financemanager.feature.category.category_impl.domain.usecase.GetCategoriesUseCase
import soft.divan.financemanager.feature.category.category_impl.domain.usecase.SearchCategoryUseCase
import soft.divan.financemanager.feature.category.category_impl.domain.usecase.impl.GetCategoriesUseCaseImpl
import soft.divan.financemanager.feature.category.category_impl.domain.usecase.impl.SearchCategoryUseCaseImpl
import soft.divan.financemanager.feature.category.category_impl.navigation.CategoryFeatureImpl


@Module
@InstallIn(SingletonComponent::class)
abstract class CategoryModule {


    @Binds
    abstract fun bindCategoryRouter(impl: CategoryFeatureImpl): CategoryFeatureApi

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