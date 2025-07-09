package soft.divan.financemanager.domain.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.category.domain.usecase.GetCategoriesUseCase
import soft.divan.financemanager.category.domain.usecase.SearchCategoryUseCase
import soft.divan.financemanager.category.domain.usecase.impl.GetCategoriesUseCaseImpl
import soft.divan.financemanager.category.domain.usecase.impl.SearchCategoryUseCaseImpl
import soft.divan.financemanager.domain.usecase.transaction.GetExpensesByPeriodUseCase
import soft.divan.financemanager.domain.usecase.transaction.GetIncomeByPeriodUseCase
import soft.divan.financemanager.domain.usecase.transaction.GetSumTransactionsUseCase
import soft.divan.financemanager.domain.usecase.transaction.GetTodayExpensesUseCase
import soft.divan.financemanager.domain.usecase.transaction.GetTodayIncomeUseCase
import soft.divan.financemanager.domain.usecase.transaction.impl.GetExpensesByPeriodUseCaseImpl
import soft.divan.financemanager.domain.usecase.transaction.impl.GetIncomeByPeriodUseCaseImpl
import soft.divan.financemanager.domain.usecase.transaction.impl.GetSumTransactionsUseCaseImpl
import soft.divan.financemanager.domain.usecase.transaction.impl.GetTodayExpensesUseCaseImpl
import soft.divan.financemanager.domain.usecase.transaction.impl.GetTodayIncomeUseCaseImpl
import soft.divan.finansemanager.account.domain.usecase.GetAccountsUseCase
import soft.divan.finansemanager.account.domain.usecase.UpdateAccountUseCase
import soft.divan.finansemanager.account.domain.usecase.UpdateCurrencyUseCase
import soft.divan.finansemanager.account.domain.usecase.impl.GetAccountsUseCaseImpl
import soft.divan.finansemanager.account.domain.usecase.impl.UpdateAccountUseCaseImpl
import soft.divan.finansemanager.account.domain.usecase.impl.UpdateCurrencyUseCaseIml

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainModule {

    @Binds
    abstract fun bindGetAccountsUseCase(
        impl: GetAccountsUseCaseImpl
    ): GetAccountsUseCase

    @Binds
    abstract fun bindUpdateAccountUseCase(
        impl: UpdateAccountUseCaseImpl
    ): UpdateAccountUseCase

    @Binds
    abstract fun bindGetTodayExpensesUseCase(
        impl: GetTodayExpensesUseCaseImpl
    ): GetTodayExpensesUseCase

    @Binds
    abstract fun bindGetSumTransactionsUseCase(
        impl: GetSumTransactionsUseCaseImpl
    ): GetSumTransactionsUseCase

    @Binds
    abstract fun bindGetExpensesByPeriodUseCase(
        impl: GetExpensesByPeriodUseCaseImpl
    ): GetExpensesByPeriodUseCase

    @Binds
    abstract fun bindGetIncomeByPeriodUseCase(
        impl: GetIncomeByPeriodUseCaseImpl
    ): GetIncomeByPeriodUseCase

    @Binds
    abstract fun bindGetTodayIncomeUseCase(
        impl: GetTodayIncomeUseCaseImpl
    ): GetTodayIncomeUseCase

    @Binds
    abstract fun bindGetCategoriesUseCase(
        impl: GetCategoriesUseCaseImpl
    ): GetCategoriesUseCase

    @Binds
    abstract fun bindSearchCategoryUseCase(
        impl: SearchCategoryUseCaseImpl
    ): SearchCategoryUseCase

    @Binds
    abstract fun bindUpdateCurrencyUseCase(
        impl: UpdateCurrencyUseCaseIml
    ): UpdateCurrencyUseCase
}

