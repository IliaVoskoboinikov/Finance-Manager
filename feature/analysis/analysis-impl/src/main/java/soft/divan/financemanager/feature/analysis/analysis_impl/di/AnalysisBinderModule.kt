package soft.divan.financemanager.feature.analysis.analysis_impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.core.domain.usecase.GetTransactionsByPeriodUseCase
import soft.divan.financemanager.core.domain.usecase.GetTransactionsByPeriodUseCaseImpl
import soft.divan.financemanager.feature.analysis.analysis_api.AnalysisFeatureApi
import soft.divan.financemanager.feature.analysis.analysis_impl.domain.usecase.GetCategoryPieChartDataUseCase
import soft.divan.financemanager.feature.analysis.analysis_impl.domain.usecase.impl.GetCategoryPieChartDataUseCaseImpl
import soft.divan.financemanager.feature.analysis.analysis_impl.navigation.AnalysisFeatureImpl

@Module
@InstallIn(SingletonComponent::class)
interface AnalysisBinderModule {
    @Binds
    fun bindTransactionRouter(impl: AnalysisFeatureImpl): AnalysisFeatureApi

    @Binds
    fun bindGetTransactionsByPeriodUseCaseImpl(impl: GetTransactionsByPeriodUseCaseImpl): GetTransactionsByPeriodUseCase

    @Binds
    fun bindGetCategoryPieChartDataUseCase(impl: GetCategoryPieChartDataUseCaseImpl): GetCategoryPieChartDataUseCase
}