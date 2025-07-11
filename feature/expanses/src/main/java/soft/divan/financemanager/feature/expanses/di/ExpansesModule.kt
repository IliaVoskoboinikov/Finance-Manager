package soft.divan.financemanager.feature.expanses.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.expanses.domain.GetExpensesByPeriodUseCase
import soft.divan.financemanager.feature.expanses.domain.GetExpensesByPeriodUseCaseImpl
import soft.divan.financemanager.feature.expanses.domain.GetTodayExpensesUseCase
import soft.divan.financemanager.feature.expanses.domain.GetTodayExpensesUseCaseImpl


@Module
@InstallIn(SingletonComponent::class)
abstract class ExpansesModule {


    @Binds
    abstract fun bindGetTodayExpensesUseCase(
        impl: GetTodayExpensesUseCaseImpl
    ): GetTodayExpensesUseCase

    @Binds
    abstract fun bindGetExpensesByPeriodUseCase(
        impl: GetExpensesByPeriodUseCaseImpl
    ): GetExpensesByPeriodUseCase


}