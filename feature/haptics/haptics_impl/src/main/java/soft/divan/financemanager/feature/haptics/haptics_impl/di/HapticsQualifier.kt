package soft.divan.financemanager.feature.haptics.haptics_impl.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class ApplicationScope

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class HapticsDataStore