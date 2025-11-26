package soft.divan.financemanager.feature.create_account.create_account_api

import soft.divan.financemanager.core.feature_api.FeatureApi

interface CreateAccountFeatureApi : FeatureApi {
    fun accountRouteWithArgs(accountId: Int? = null): String
}