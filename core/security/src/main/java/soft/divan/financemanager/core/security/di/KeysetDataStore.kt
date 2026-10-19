package soft.divan.financemanager.core.security.di

import javax.inject.Qualifier

/** DataStore с набором ключей базы — см. [EncryptionModule.provideKeysetDataStore]. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class KeysetDataStore
