package soft.divan.financemanager.feature.security.impl.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.security.impl.data.sourse.impl.SecurityLocalDataSourceImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityProviderModule {

    @Provides
    @Singleton
    fun provideSecurityLocalDataSourceImpl(
        @ApplicationContext context: Context
    ): SecurityLocalDataSourceImpl = SecurityLocalDataSourceImpl(context)
}
// Revue me>>
