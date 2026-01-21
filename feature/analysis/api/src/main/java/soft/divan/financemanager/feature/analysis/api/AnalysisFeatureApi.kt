package soft.divan.financemanager.feature.analysis.api

import soft.divan.financemanager.core.featureapi.FeatureApi

interface AnalysisFeatureApi : FeatureApi {
    fun analysisRouteWithArgs(isIncome: Boolean): String
}