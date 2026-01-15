package soft.divan.financemanager.feature.transaction.transaction_api

import soft.divan.financemanager.core.feature_api.FeatureApi

interface TransactionFeatureApi : FeatureApi {
    fun transactionRouteWithArgs(transactionId: String, isIncome: Boolean): String
    fun transactionRouteWithArgs(isIncome: Boolean): String
}