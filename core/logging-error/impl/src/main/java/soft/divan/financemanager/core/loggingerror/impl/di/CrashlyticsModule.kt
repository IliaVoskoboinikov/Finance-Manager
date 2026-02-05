package soft.divan.financemanager.core.loggingerror.impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.core.loggingerror.api.ErrorLogger
import soft.divan.financemanager.core.loggingerror.impl.CrashlyticsLogger

@Module
@InstallIn(SingletonComponent::class)
interface CrashlyticsModule {

    @Binds
    fun bindErrorLogger(impl: CrashlyticsLogger): ErrorLogger
}
