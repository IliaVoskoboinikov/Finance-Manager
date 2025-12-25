package soft.divan.financemanager.feature.languages.languages_impl.domain.locale

import soft.divan.financemanager.feature.languages.languages_impl.domain.model.Language

interface AppLocaleManager {
    fun apply(language: Language)
    fun getCurrent(): Language
}