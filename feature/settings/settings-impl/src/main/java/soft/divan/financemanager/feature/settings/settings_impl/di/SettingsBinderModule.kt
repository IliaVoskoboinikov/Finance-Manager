package soft.divan.financemanager.feature.settings.settings_impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.settings.settings_api.SettingsFeatureApi
import soft.divan.financemanager.feature.settings.settings_impl.navigation.SettingsFeatureImpl

@Module
@InstallIn(SingletonComponent::class)
interface SettingsBinderModule {

    @Binds
    fun bindSettingsRouter(impl: SettingsFeatureImpl): SettingsFeatureApi

}
