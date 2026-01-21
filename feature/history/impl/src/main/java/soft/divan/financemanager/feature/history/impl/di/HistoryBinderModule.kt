package soft.divan.financemanager.feature.history.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.history.api.HistoryFeatureApi
import soft.divan.financemanager.feature.history.impl.navigation.HistoryFeatureImpl

@Module
@InstallIn(SingletonComponent::class)
interface HistoryBinderModule {

    @Binds
    fun bindHistoryRouter(impl: HistoryFeatureImpl): HistoryFeatureApi
}
