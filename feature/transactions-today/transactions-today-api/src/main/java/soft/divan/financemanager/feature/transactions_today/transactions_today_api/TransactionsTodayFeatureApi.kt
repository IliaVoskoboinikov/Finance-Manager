package soft.divan.financemanager.feature.transactions_today.transactions_today_api

import soft.divan.financemanager.core.feature_api.FeatureApi

interface TransactionsTodayFeatureApi : FeatureApi {
    val route2: String
    fun transactionsTodayRouteWithArgs(isIncome: Boolean): String
}