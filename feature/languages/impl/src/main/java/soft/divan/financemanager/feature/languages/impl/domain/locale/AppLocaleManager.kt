package soft.divan.financemanager.feature.languages.impl.domain.locale

import soft.divan.financemanager.feature.languages.impl.domain.model.Language

interface AppLocaleManager {
    fun apply(language: Language)
    fun getCurrent(): Language
}