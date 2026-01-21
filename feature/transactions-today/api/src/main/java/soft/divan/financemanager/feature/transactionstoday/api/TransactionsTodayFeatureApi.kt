package soft.divan.financemanager.feature.transactionstoday.api

import soft.divan.financemanager.core.featureapi.FeatureApi

interface TransactionsTodayFeatureApi : FeatureApi {
    val route2: String
    fun transactionsTodayRouteWithArgs(isIncome: Boolean): String
}