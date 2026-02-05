package soft.divan.financemanager.feature.transaction.api

import soft.divan.financemanager.core.featureapi.FeatureApi

interface TransactionFeatureApi : FeatureApi {
    fun transactionRouteWithArgs(transactionId: String, isIncome: Boolean): String
    fun transactionRouteWithArgs(isIncome: Boolean): String
}
