package soft.divan.financemanager.core.network.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.core.network.serialization.BigDecimalTypeAdapter
import java.math.BigDecimal
import javax.inject.Singleton

/**
 * (Де)сериализация JSON. Точность денег обеспечивается [BigDecimalTypeAdapter]:
 * [BigDecimal] вместо `Double`, поэтому десятичные суммы не искажаются.
 */
@Module
@InstallIn(SingletonComponent::class)
object SerializationModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder()
        .registerTypeAdapter(BigDecimal::class.java, BigDecimalTypeAdapter())
        .create()
}
