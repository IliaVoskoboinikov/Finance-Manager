package soft.divan.financemanager.feature.history.history_api

import soft.divan.financemanager.core.feature_api.FeatureApi

interface HistoryFeatureApi : FeatureApi {
    fun transactionRouteWithArgs(isIncome: Boolean): String
}