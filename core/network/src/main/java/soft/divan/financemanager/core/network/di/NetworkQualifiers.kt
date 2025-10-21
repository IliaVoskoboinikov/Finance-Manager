package soft.divan.financemanager.core.network.di


import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthInterceptorQualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class NetworkInterceptorQualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RetryInterceptorQualifier