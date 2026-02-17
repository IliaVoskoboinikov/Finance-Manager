package soft.divan.financemanager.core.loggingerror.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.core.loggingerror.ErrorLogger
import soft.divan.financemanager.core.loggingerror.CrashlyticsLogger

@Module
@InstallIn(SingletonComponent::class)
interface CrashlyticsModule {

    @Binds
    fun bindErrorLogger(impl: CrashlyticsLogger): ErrorLogger
}
