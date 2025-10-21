package soft.divan.financemanager.feature.expenses.expenses_impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.expenses.expenses_api.ExpensesFeatureApi
import soft.divan.financemanager.feature.expenses.expenses_impl.domain.GetExpensesByPeriodUseCase
import soft.divan.financemanager.feature.expenses.expenses_impl.domain.GetExpensesByPeriodUseCaseImpl
import soft.divan.financemanager.feature.expenses.expenses_impl.domain.GetTodayExpensesUseCase
import soft.divan.financemanager.feature.expenses.expenses_impl.domain.GetTodayExpensesUseCaseImpl
import soft.divan.financemanager.feature.expenses.expenses_impl.navigation.ExpensesFeatureImpl


@Module
@InstallIn(SingletonComponent::class)
interface ExpensesModule {

    @Binds
    fun bindExpensesRouter(impl: ExpensesFeatureImpl): ExpensesFeatureApi

    @Binds
    fun bindGetTodayExpensesUseCase(impl: GetTodayExpensesUseCaseImpl): GetTodayExpensesUseCase

    @Binds
    fun bindGetExpensesByPeriodUseCase(impl: GetExpensesByPeriodUseCaseImpl): GetExpensesByPeriodUseCase

}