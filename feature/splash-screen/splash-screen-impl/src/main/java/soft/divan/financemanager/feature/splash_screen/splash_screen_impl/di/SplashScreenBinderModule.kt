package soft.divan.financemanager.feature.splash_screen.splash_screen_impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.splash_screen.splash_screen_api.SplashScreenFeatureApi
import soft.divan.financemanager.feature.splash_screen.splash_screen_impl.navigation.SplashScreenFeatureImpl

@Module
@InstallIn(SingletonComponent::class)
interface SplashScreenBinderModule {

    @Binds
    fun bindSplashScreenRouter(impl: SplashScreenFeatureImpl): SplashScreenFeatureApi

}
