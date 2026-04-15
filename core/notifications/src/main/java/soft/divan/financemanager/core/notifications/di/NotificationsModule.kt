package soft.divan.financemanager.core.notifications.di

import android.content.Context
import androidx.work.WorkManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.core.notifications.NotificationHelper
import soft.divan.financemanager.core.notifications.NotificationHelperImpl
import soft.divan.financemanager.core.notifications.worker.sheduler.NotificationScheduler
import soft.divan.financemanager.core.notifications.worker.sheduler.NotificationSchedulerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface NotificationsModule {

    @Binds
    @Singleton
    fun bindNotificationHelper(
        impl: NotificationHelperImpl
    ): NotificationHelper

    @Binds
    @Singleton
    fun bindNotificationScheduler(
        impl: NotificationSchedulerImpl
    ): NotificationScheduler
    companion object {
        @Provides
        @Singleton
        fun provideWorkManager(@ApplicationContext context: Context): WorkManager {
            return WorkManager.getInstance(context)
        }
    }
}
