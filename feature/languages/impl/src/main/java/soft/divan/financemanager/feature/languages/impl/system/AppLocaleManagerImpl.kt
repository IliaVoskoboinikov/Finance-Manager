package soft.divan.financemanager.feature.languages.impl.system

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import soft.divan.financemanager.feature.languages.impl.data.mapper.toDomain
import soft.divan.financemanager.feature.languages.impl.domain.locale.AppLocaleManager
import soft.divan.financemanager.feature.languages.impl.domain.model.Language
import java.util.Locale
import javax.inject.Inject

class AppLocaleManagerImpl @Inject constructor(
    @param:ApplicationContext private val context: Context
) : AppLocaleManager {

    override fun apply(language: Language) {
        val locales = LocaleListCompat.forLanguageTags(language.tag)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(LocaleManager::class.java).applicationLocales =
                LocaleList.forLanguageTags(language.tag)
        } else {
            AppCompatDelegate.setApplicationLocales(locales)
        }
    }

    override fun getCurrent(): Language {
        val locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getLocaleForTiramisu()
        } else {
            getLocalePreTiramisu()
        }

        return locale.toDomain()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun getLocaleForTiramisu(): Locale {
        val appLocales = context.getSystemService(LocaleManager::class.java)?.applicationLocales
        return appLocales?.takeIf { !it.isEmpty }?.get(0) ?: context.resources.configuration.locales[0]
    }

    private fun getLocalePreTiramisu(): Locale {
        val appLocales = AppCompatDelegate.getApplicationLocales()
        return appLocales.takeIf { !it.isEmpty }?.get(0) ?: Locale.getDefault()
    }
}
