package soft.divan.financemanager.domain.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.domain.usecase.account.GetAccountsUseCase
import soft.divan.financemanager.domain.usecase.account.UpdateAccountUseCase
import soft.divan.financemanager.domain.usecase.account.impl.GetAccountsUseCaseImpl
import soft.divan.financemanager.domain.usecase.account.impl.UpdateAccountUseCaseImpl
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

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {


    @Provides
    fun provideGetAccountsUseCaseImpl(
        impl: GetAccountsUseCaseImpl
    ): GetAccountsUseCase {
        return impl
    }

    @Provides
    fun provideUpdateAccountsUseCaseImpl(
        impl: UpdateAccountUseCaseImpl
    ): UpdateAccountUseCase {
        return impl
    }

    @Provides
    fun provideGetTodayExpensesUseCaseImpl(
        impl: GetTodayExpensesUseCaseImpl
    ): GetTodayExpensesUseCase {
        return impl
    }

    @Provides
    fun provideGetSumTransactionsUseCaseImpl(
        impl: GetSumTransactionsUseCaseImpl
    ): GetSumTransactionsUseCase {
        return impl
    }

    @Provides
    fun provideGetExpensesByPeriodUseCaseImpl(
        impl: GetExpensesByPeriodUseCaseImpl
    ): GetExpensesByPeriodUseCase {
        return impl
    }

    @Provides
    fun provideGetIncomeByPeriodUseCaseImpl(
        impl: GetIncomeByPeriodUseCaseImpl
    ): GetIncomeByPeriodUseCase {
        return impl
    }

    @Provides
    fun provideGetTodayIncomeUseCaseImpl(
        impl: GetTodayIncomeUseCaseImpl
    ): GetTodayIncomeUseCase {
        return impl
    }


}