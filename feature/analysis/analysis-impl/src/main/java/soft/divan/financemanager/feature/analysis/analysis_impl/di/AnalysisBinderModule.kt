package soft.divan.financemanager.feature.analysis.analysis_impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.core.domain.usecase.GetTransactionByPeriodUseCase
import soft.divan.financemanager.core.domain.usecase.GetTransactionByPeriodUseCaseImpl
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
    fun bindGetTransactionByPeriodUseCaseImpl(impl: GetTransactionByPeriodUseCaseImpl): GetTransactionByPeriodUseCase

    @Binds
    fun bindGetCategoryPieChartDataUseCase(impl: GetCategoryPieChartDataUseCaseImpl): GetCategoryPieChartDataUseCase
}