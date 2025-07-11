package soft.divan.financemanager.feature.settings.settings_api

import soft.divan.financemanager.core.feature_api.FeatureApi

interface SettingsFeatureApi : FeatureApi {
    val settingsRoute: String
}