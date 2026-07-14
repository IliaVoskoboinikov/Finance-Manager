package soft.divan.financemanager.feature.auth.impl.di

import android.content.Context
import com.yandex.authsdk.YandexAuthOptions
import com.yandex.authsdk.YandexAuthSdk
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Предоставляет [YandexAuthSdk] для OAuth-входа через Яндекс.
 *
 * client_id берётся из meta-data манифеста (плейсхолдер `YANDEX_CLIENT_ID`,
 * задаётся в build.gradle модуля), поэтому в коде его указывать не нужно.
 */
@Module
@InstallIn(SingletonComponent::class)
object YandexAuthModule {

    @Provides
    @Singleton
    fun provideYandexAuthSdk(
        @ApplicationContext context: Context
    ): YandexAuthSdk = YandexAuthSdk.create(YandexAuthOptions(context))
}
