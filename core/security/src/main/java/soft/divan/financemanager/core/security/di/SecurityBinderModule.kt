package soft.divan.financemanager.core.security.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.core.security.CryptoManager
import soft.divan.financemanager.core.security.impl.AndroidCryptoManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface SecurityBinderModule {
    @Binds
    @Singleton
    fun bindCryptoManager(impl: AndroidCryptoManager): CryptoManager
}
