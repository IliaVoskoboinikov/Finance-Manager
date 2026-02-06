package soft.divan.financemanager.feature.transactionstoday.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.transactionstoday.api.TransactionsTodayFeatureApi
import soft.divan.financemanager.feature.transactionstoday.impl.navigation.TransactionsTodayFeatureImpl

@Module
@InstallIn(SingletonComponent::class)
interface TransactionsTodayBinderModule {

    @Binds
    fun bindTransactionsTodayRouter(impl: TransactionsTodayFeatureImpl): TransactionsTodayFeatureApi
}
// Revue me>>
