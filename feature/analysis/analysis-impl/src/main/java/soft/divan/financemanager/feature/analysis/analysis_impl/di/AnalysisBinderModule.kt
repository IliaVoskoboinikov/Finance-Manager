package soft.divan.financemanager.feature.analysis.analysis_impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.analysis.analysis_api.AnalysisFeatureApi
import soft.divan.financemanager.feature.analysis.analysis_impl.navigation.AnalysisFeatureImpl

@Module
@InstallIn(SingletonComponent::class)
interface AnalysisBinderModule {
    @Binds
    fun bindTransactionRouter(impl: AnalysisFeatureImpl): AnalysisFeatureApi
}