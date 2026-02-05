package soft.divan.financemanager.feature.languages.impl.precenter.mapper

import soft.divan.financemanager.feature.languages.impl.domain.model.Language
import soft.divan.financemanager.feature.languages.impl.precenter.model.LanguageUi

fun Language.toUi(): LanguageUi =
    when (this) {
        Language.ENGLISH -> LanguageUi.ENGLISH
        Language.RUSSIAN -> LanguageUi.RUSSIAN
    }

fun LanguageUi.toDomain(): Language =
    when (this) {
        LanguageUi.ENGLISH -> Language.ENGLISH
        LanguageUi.RUSSIAN -> Language.RUSSIAN
    }
