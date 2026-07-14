package soft.divan.financemanager.feature.languages.impl.system

import android.app.LocaleManager
import android.content.Context
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.test.core.app.ApplicationProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import soft.divan.financemanager.feature.languages.impl.domain.model.Language
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
class AppLocaleManagerImplTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val manager = AppLocaleManagerImpl(context)

    @After
    fun tearDown() {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
    }

    @Test
    @Config(sdk = [33])
    fun `apply on Tiramisu stores locales via LocaleManager`() {
        manager.apply(Language.RUSSIAN)

        val locales = context.getSystemService(LocaleManager::class.java).applicationLocales
        assertThat(locales.toLanguageTags()).isEqualTo("ru")
    }

    @Test
    @Config(sdk = [33])
    fun `getCurrent on Tiramisu reads applied language back`() {
        context.getSystemService(LocaleManager::class.java).applicationLocales =
            LocaleList.forLanguageTags("ru")

        assertThat(manager.getCurrent()).isEqualTo(Language.RUSSIAN)
    }

    @Test
    @Config(sdk = [32])
    fun `apply below Tiramisu stores locales via AppCompatDelegate`() {
        manager.apply(Language.ENGLISH)

        assertThat(AppCompatDelegate.getApplicationLocales().toLanguageTags()).isEqualTo("en")
    }

    @Test
    @Config(sdk = [32])
    fun `getCurrent below Tiramisu reads applied language back`() {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("ru"))

        assertThat(manager.getCurrent()).isEqualTo(Language.RUSSIAN)
    }

    @Test
    @Config(sdk = [32])
    fun `getCurrent falls back to default locale when nothing applied`() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale("de"))

            // неизвестный язык системы схлопывается в ENGLISH
            assertThat(manager.getCurrent()).isEqualTo(Language.ENGLISH)
        } finally {
            Locale.setDefault(previous)
        }
    }
}
