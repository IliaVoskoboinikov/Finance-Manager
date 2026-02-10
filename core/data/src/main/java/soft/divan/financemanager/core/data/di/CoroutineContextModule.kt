package soft.divan.financemanager.core.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import soft.divan.common.di.ApplicationScope
import soft.divan.common.di.IoDispatcher
import soft.divan.financemanager.core.data.util.coroutne.AppCoroutineContext
import soft.divan.financemanager.core.data.util.coroutne.impl.DefaultAppCoroutineContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoroutineContextModule {

    @Provides
    @Singleton
    fun provideAppCoroutineContext(
        @ApplicationScope scope: CoroutineScope,
        @IoDispatcher dispatcher: CoroutineDispatcher,
        exceptionHandler: CoroutineExceptionHandler
    ): AppCoroutineContext {
        return DefaultAppCoroutineContext(
            scope = scope,
            dispatcher = dispatcher,
            exceptionHandler = exceptionHandler
        )
    }
}
// Revue me>>
