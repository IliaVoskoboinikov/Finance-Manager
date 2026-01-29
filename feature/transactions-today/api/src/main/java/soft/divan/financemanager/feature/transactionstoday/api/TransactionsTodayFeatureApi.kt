package soft.divan.financemanager.feature.transactionstoday.api

import soft.divan.financemanager.core.featureapi.FeatureApi

interface TransactionsTodayFeatureApi : FeatureApi {
    val expenseRoute: String
    val incomeRoute: String
}