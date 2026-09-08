package soft.divan.financemanager.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import soft.divan.financemanager.core.featureapi.FeatureApi
import soft.divan.financemanager.feature.account.api.AccountFeatureApi
import soft.divan.financemanager.feature.analysis.api.AnalysisFeatureApi
import soft.divan.financemanager.feature.auth.api.AuthFeatureApi
import soft.divan.financemanager.feature.category.api.CategoryFeatureApi
import soft.divan.financemanager.feature.designapp.api.DesignAppFeatureApi
import soft.divan.financemanager.feature.haptics.api.HapticsFeatureApi
import soft.divan.financemanager.feature.history.api.HistoryFeatureApi
import soft.divan.financemanager.feature.languages.api.LanguagesFeatureApi
import soft.divan.financemanager.feature.myaccounts.impl.MyAccountsFeatureApi
import soft.divan.financemanager.feature.security.api.SecurityFeatureApi
import soft.divan.financemanager.feature.settings.api.SettingsFeatureApi
import soft.divan.financemanager.feature.sounds.api.SoundsFeatureApi
import soft.divan.financemanager.feature.synchronization.api.SynchronizationFeatureApi
import soft.divan.financemanager.feature.transaction.api.TransactionFeatureApi
import soft.divan.financemanager.feature.transactionstoday.api.TransactionsTodayFeatureApi

/**
 * Состав основного навигационного графа приложения.
 *
 * Каждая фича попадает в граф ровно один раз: `app` собирает `Set<FeatureApi>` и просит
 * каждую фичу добавить свои `NavEntry` в общий `entryProvider`. Сюда входят все экраны,
 * доступные после авторизации, — и вкладки нижней навигации, и вложенные экраны.
 *
 * Экраны корневого графа (splash и авторизация как точка входа) в набор не входят: они
 * живут в собственном back stack и регистрируются явно в `RootNavDisplay`.
 */
@Module
@InstallIn(SingletonComponent::class)
@Suppress("TooManyFunctions") // Декларативный список фич графа: по одной привязке на фичу.
interface FeatureNavigationModule {

    @Binds
    @IntoSet
    fun bindTransactionsTodayFeature(impl: TransactionsTodayFeatureApi): FeatureApi

    @Binds
    @IntoSet
    fun bindTransactionFeature(impl: TransactionFeatureApi): FeatureApi

    @Binds
    @IntoSet
    fun bindHistoryFeature(impl: HistoryFeatureApi): FeatureApi

    @Binds
    @IntoSet
    fun bindAnalysisFeature(impl: AnalysisFeatureApi): FeatureApi

    @Binds
    @IntoSet
    fun bindMyAccountsFeature(impl: MyAccountsFeatureApi): FeatureApi

    @Binds
    @IntoSet
    fun bindAccountFeature(impl: AccountFeatureApi): FeatureApi

    @Binds
    @IntoSet
    fun bindCategoryFeature(impl: CategoryFeatureApi): FeatureApi

    @Binds
    @IntoSet
    fun bindSettingsFeature(impl: SettingsFeatureApi): FeatureApi

    @Binds
    @IntoSet
    fun bindSecurityFeature(impl: SecurityFeatureApi): FeatureApi

    @Binds
    @IntoSet
    fun bindDesignAppFeature(impl: DesignAppFeatureApi): FeatureApi

    @Binds
    @IntoSet
    fun bindHapticsFeature(impl: HapticsFeatureApi): FeatureApi

    @Binds
    @IntoSet
    fun bindSoundsFeature(impl: SoundsFeatureApi): FeatureApi

    @Binds
    @IntoSet
    fun bindLanguagesFeature(impl: LanguagesFeatureApi): FeatureApi

    @Binds
    @IntoSet
    fun bindSynchronizationFeature(impl: SynchronizationFeatureApi): FeatureApi

    @Binds
    @IntoSet
    fun bindAuthFeature(impl: AuthFeatureApi): FeatureApi
}
