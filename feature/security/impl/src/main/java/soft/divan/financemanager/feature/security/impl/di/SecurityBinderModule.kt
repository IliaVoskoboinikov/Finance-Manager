package soft.divan.financemanager.feature.security.impl.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.feature.security.api.SecurityFeatureApi
import soft.divan.financemanager.feature.security.impl.data.repository.SecureScreenRepositoryImpl
import soft.divan.financemanager.feature.security.impl.data.repository.SecurityRepositoryImpl
import soft.divan.financemanager.feature.security.impl.data.sourse.SecureScreenLocalDataSource
import soft.divan.financemanager.feature.security.impl.data.sourse.SecurityLocalDataSource
import soft.divan.financemanager.feature.security.impl.data.sourse.impl.SecureScreenLocalDataSourceImpl
import soft.divan.financemanager.feature.security.impl.data.sourse.impl.SecurityLocalDataSourceImpl
import soft.divan.financemanager.feature.security.impl.domain.repository.SecureScreenRepository
import soft.divan.financemanager.feature.security.impl.domain.repository.SecurityRepository
import soft.divan.financemanager.feature.security.impl.navigation.SecurityFeatureImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface SecurityBinderModule {

    @Binds
    fun bindSecurityRouter(impl: SecurityFeatureImpl): SecurityFeatureApi

    @Binds
    @Singleton
    fun provideSecurityRepository(impl: SecurityRepositoryImpl): SecurityRepository

    @Binds
    @Singleton
    fun bindSecurityLocalDataSource(impl: SecurityLocalDataSourceImpl): SecurityLocalDataSource

    @Binds
    @Singleton
    fun bindSecureScreenRepository(impl: SecureScreenRepositoryImpl): SecureScreenRepository

    @Binds
    @Singleton
    fun bindSecureScreenLocalDataSource(
        impl: SecureScreenLocalDataSourceImpl
    ): SecureScreenLocalDataSource

    companion object {

        @Provides
        @Singleton
        @SecuritySettingsDataStore
        fun provideSecuritySettingsDataStore(
            @ApplicationContext context: Context
        ): DataStore<Preferences> = context.securitySettingsDataStore
    }
}
