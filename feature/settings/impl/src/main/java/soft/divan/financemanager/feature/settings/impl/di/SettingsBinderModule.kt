package soft.divan.financemanager.feature.settings.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.settings.api.SettingsFeatureApi
import soft.divan.financemanager.feature.settings.impl.navigation.SettingsFeatureImpl

@Module
@InstallIn(SingletonComponent::class)
interface SettingsBinderModule {

    @Binds
    fun bindSettingsRouter(impl: SettingsFeatureImpl): SettingsFeatureApi
}
// Revue me>>
