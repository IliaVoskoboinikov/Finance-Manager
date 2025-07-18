package soft.divan.financemanager.feature.category.category_impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.category.category_api.CategoryFeatureApi
import soft.divan.financemanager.feature.category.category_impl.domain.usecase.SearchCategoryUseCase
import soft.divan.financemanager.feature.category.category_impl.domain.usecase.impl.SearchCategoryUseCaseImpl
import soft.divan.financemanager.feature.category.category_impl.navigation.CategoryFeatureImpl


@Module
@InstallIn(SingletonComponent::class)
abstract class CategoryModule {


    @Binds
    abstract fun bindCategoryRouter(impl: CategoryFeatureImpl): CategoryFeatureApi


    @Binds
    abstract fun bindSearchCategoryUseCase(
        impl: SearchCategoryUseCaseImpl
    ): SearchCategoryUseCase


}