package soft.divan.financemanager.core.logging_error.logging_error_impl.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.core.logging_error.logging_error_api.ErrorLogger
import soft.divan.financemanager.core.logging_error.logging_error_impl.CrashlyticsLogger

@Module
@InstallIn(SingletonComponent::class)
interface CrashlyticsModule {

    @Binds
    fun bindErrorLogger(impl: CrashlyticsLogger): ErrorLogger

}
