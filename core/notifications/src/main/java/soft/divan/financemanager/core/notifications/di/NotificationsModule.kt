package soft.divan.financemanager.core.notifications.di

import com.google.firebase.messaging.FirebaseMessaging
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import soft.divan.financemanager.core.notifications.NotificationHelper
import soft.divan.financemanager.core.notifications.NotificationHelperImpl
import soft.divan.financemanager.core.notifications.fcm.PushSubscriptionManager
import soft.divan.financemanager.core.notifications.fcm.PushSubscriptionManagerImpl
import soft.divan.financemanager.core.notifications.scheduler.InactivityReminderScheduler
import soft.divan.financemanager.core.notifications.scheduler.InactivityReminderSchedulerImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
interface NotificationsModule {

    @Binds
    @Singleton
    fun bindNotificationHelper(impl: NotificationHelperImpl): NotificationHelper

    @Binds
    @Singleton
    fun bindInactivityReminderScheduler(
        impl: InactivityReminderSchedulerImpl
    ): InactivityReminderScheduler

    @Binds
    @Singleton
    fun bindPushSubscriptionManager(
        impl: PushSubscriptionManagerImpl
    ): PushSubscriptionManager

    companion object {

        /**
         * `@Provides`: [FirebaseMessaging] — внешний класс со статической фабрикой.
         * Инъекция вместо `getInstance()` по месту делает [PushSubscriptionManagerImpl]
         * тестируемым без Firebase-рантайма.
         */
        @Provides
        @Singleton
        fun provideFirebaseMessaging(): FirebaseMessaging = FirebaseMessaging.getInstance()
    }
}
