package soft.divan.financemanager.feature.history.history_impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.history.history_api.HistoryFeatureApi
import soft.divan.financemanager.feature.history.history_impl.domain.usecase.GetTransactionsByPeriodUseCase
import soft.divan.financemanager.feature.history.history_impl.domain.usecase.impl.GetTransactionsByPeriodUseCaseImpl
import soft.divan.financemanager.feature.history.history_impl.navigation.HistoryFeatureImpl

@Module
@InstallIn(SingletonComponent::class)
interface HistoryBinderModule {

    @Binds
    fun bindHistoryRouter(impl: HistoryFeatureImpl): HistoryFeatureApi

    @Binds
    fun bindGetTransactionsByPeriodUseCase(impl: GetTransactionsByPeriodUseCaseImpl): GetTransactionsByPeriodUseCase
}
