package soft.divan.financemanager.feature.category.category_impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.core.domain.usecase.GetCategoriesUseCase
import soft.divan.financemanager.core.domain.usecase.GetCategoriesUseCaseImpl
import soft.divan.financemanager.feature.category.category_api.CategoryFeatureApi
import soft.divan.financemanager.feature.category.category_impl.domain.usecase.SearchCategoryUseCase
import soft.divan.financemanager.feature.category.category_impl.domain.usecase.impl.SearchCategoryUseCaseImpl
import soft.divan.financemanager.feature.category.category_impl.navigation.CategoryFeatureImpl


@Module
@InstallIn(SingletonComponent::class)
interface CategoryBinderModule {

    @Binds
    fun bindCategoryRouter(impl: CategoryFeatureImpl): CategoryFeatureApi

    @Binds
    fun bindSearchCategoryUseCase(impl: SearchCategoryUseCaseImpl): SearchCategoryUseCase

    @Binds
    fun bindGetCategoriesUseCase(impl: GetCategoriesUseCaseImpl): GetCategoriesUseCase

}