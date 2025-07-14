package soft.divan.financemanager.feature.income.income_impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.income.income_api.IncomeFeatureApi
import soft.divan.financemanager.feature.income.income_impl.domain.usecase.GetIncomeByPeriodUseCase
import soft.divan.financemanager.feature.income.income_impl.domain.usecase.GetIncomeByPeriodUseCaseImpl
import soft.divan.financemanager.feature.income.income_impl.domain.usecase.GetTodayIncomeUseCase
import soft.divan.financemanager.feature.income.income_impl.domain.usecase.GetTodayIncomeUseCaseImpl
import soft.divan.financemanager.feature.income.income_impl.navigation.IncomeFeatureImpl


@Module
@InstallIn(SingletonComponent::class)
abstract class IncomeModule {


    @Binds
    abstract fun bindIncomeRouter(impl: IncomeFeatureImpl): IncomeFeatureApi


    @Binds
    abstract fun bindGetIncomeByPeriodUseCase(
        impl: GetIncomeByPeriodUseCaseImpl
    ): GetIncomeByPeriodUseCase

    @Binds
    abstract fun bindGetTodayIncomeUseCase(
        impl: GetTodayIncomeUseCaseImpl
    ): GetTodayIncomeUseCase


}

