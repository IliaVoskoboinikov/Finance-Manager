package soft.divan.financemanager.feature.history.api

import soft.divan.financemanager.core.featureapi.FeatureApi

interface HistoryFeatureApi : FeatureApi {
    fun transactionRouteWithArgs(isIncome: Boolean): String
}
// Revue me>>
