package soft.divan.financemanager.feature.analysis.analysis_api

import soft.divan.financemanager.core.feature_api.FeatureApi

interface AnalysisFeatureApi : FeatureApi {
    fun analysisRouteWithArgs(isIncome: Boolean): String
}