package soft.divan.financemanager.feature.transactions_today.transactions_today_impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.transactions_today.transactions_today_api.TransactionsTodayFeatureApi
import soft.divan.financemanager.feature.transactions_today.transactions_today_impl.domain.GetTodayTransactionsUseCase
import soft.divan.financemanager.feature.transactions_today.transactions_today_impl.domain.GetTodayTransactionsUseCaseImpl
import soft.divan.financemanager.feature.transactions_today.transactions_today_impl.navigation.TransactionsTodayFeatureImpl


@Module
@InstallIn(SingletonComponent::class)
interface TransactionsTodayBinderModule {

    @Binds
    fun bindTransactionsTodayRouter(impl: TransactionsTodayFeatureImpl): TransactionsTodayFeatureApi

    @Binds
    fun bindGetTodayTransactionsUseCase(impl: GetTodayTransactionsUseCaseImpl): GetTodayTransactionsUseCase


}