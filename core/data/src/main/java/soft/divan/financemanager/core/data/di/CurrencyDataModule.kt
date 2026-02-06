package soft.divan.financemanager.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.core.data.repository.CurrencyRepositoryImpl
import soft.divan.financemanager.core.domain.repository.CurrencyRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface CurrencyDataModule {

    @Binds
    @Singleton
    fun bindCurrencyRepository(
        impl: CurrencyRepositoryImpl
    ): CurrencyRepository
}
// Revue me>>
