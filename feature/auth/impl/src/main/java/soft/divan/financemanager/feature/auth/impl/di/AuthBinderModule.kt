package soft.divan.financemanager.feature.auth.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.auth.api.AuthFeatureApi
import soft.divan.financemanager.feature.auth.impl.navigation.AuthFeatureImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface AuthBinderModule {

    @Binds
    @Singleton
    fun bindAuthFeatureApi(impl: AuthFeatureImpl): AuthFeatureApi
}
