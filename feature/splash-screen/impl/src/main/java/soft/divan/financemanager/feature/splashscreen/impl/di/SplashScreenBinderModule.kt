package soft.divan.financemanager.feature.splashscreen.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.splashscreen.api.SplashScreenFeatureApi
import soft.divan.financemanager.feature.splashscreen.impl.navigation.SplashScreenFeatureImpl

@Module
@InstallIn(SingletonComponent::class)
interface SplashScreenBinderModule {

    @Binds
    fun bindSplashScreenRouter(impl: SplashScreenFeatureImpl): SplashScreenFeatureApi
}
