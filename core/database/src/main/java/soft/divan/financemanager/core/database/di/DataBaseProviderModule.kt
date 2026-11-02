package soft.divan.financemanager.core.database.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.core.database.holder.DatabaseFactory
import soft.divan.financemanager.core.database.holder.SqlCipherDatabaseFactory

/**
 * База больше не синглтон Hilt: её открывает и закрывает `DatabaseHolder` (провайдер — в
 * `:core:data`, рядом с ключами). Отсюда — только способ открыть файл.
 */
@Module
@InstallIn(SingletonComponent::class)
interface DataBaseProviderModule {

    @Binds
    fun bindDatabaseFactory(impl: SqlCipherDatabaseFactory): DatabaseFactory
}
