package soft.divan.financemanager.feature.account.account_api

import soft.divan.financemanager.core.feature_api.FeatureApi

interface AccountFeatureApi : FeatureApi {
    fun accountRouteWithArgs(accountId: Int): String
}