package soft.divan.financemanager.feature.account.api

import soft.divan.financemanager.core.featureapi.FeatureApi

interface AccountFeatureApi : FeatureApi {
    fun accountRouteWithArgs(accountId: String): String
}
