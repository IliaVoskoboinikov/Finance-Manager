package soft.divan.financemanager.feature.income.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.income.domain.usecase.GetIncomeByPeriodUseCase
import soft.divan.financemanager.feature.income.domain.usecase.GetIncomeByPeriodUseCaseImpl
import soft.divan.financemanager.feature.income.domain.usecase.GetTodayIncomeUseCase
import soft.divan.financemanager.feature.income.domain.usecase.GetTodayIncomeUseCaseImpl


@Module
@InstallIn(SingletonComponent::class)
abstract class IncomeModule {


    @Binds
    abstract fun bindGetIncomeByPeriodUseCase(
        impl: GetIncomeByPeriodUseCaseImpl
    ): GetIncomeByPeriodUseCase

    @Binds
    abstract fun bindGetTodayIncomeUseCase(
        impl: GetTodayIncomeUseCaseImpl
    ): GetTodayIncomeUseCase


}

