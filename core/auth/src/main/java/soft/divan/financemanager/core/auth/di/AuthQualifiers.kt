package soft.divan.financemanager.core.auth.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseRetrofitQualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GuestInterceptorQualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SessionDataStore

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class TokenDataStore
