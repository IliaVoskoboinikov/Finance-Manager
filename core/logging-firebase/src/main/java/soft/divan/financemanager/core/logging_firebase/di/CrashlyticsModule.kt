package soft.divan.financemanager.core.logging_firebase.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.core.logging.ErrorLogger
import soft.divan.financemanager.core.logging_firebase.CrashlyticsLogger

@Module
@InstallIn(SingletonComponent::class)
interface CrashlyticsModule {

    @Binds
    fun bindErrorLogger(impl: CrashlyticsLogger): ErrorLogger

}
