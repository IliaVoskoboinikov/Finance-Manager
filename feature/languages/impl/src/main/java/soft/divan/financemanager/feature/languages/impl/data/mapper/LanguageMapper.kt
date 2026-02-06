package soft.divan.financemanager.feature.languages.impl.data.mapper

import soft.divan.financemanager.feature.languages.impl.domain.model.Language
import java.util.Locale

fun String?.toDomain(): Language? = when {
    this == Language.RUSSIAN.tag -> Language.RUSSIAN
    this == Language.ENGLISH.tag -> Language.ENGLISH
    else -> null
}

fun Locale?.toDomain(): Language =
    when {
        this == null -> Language.ENGLISH
        this.language == Language.RUSSIAN.tag -> Language.RUSSIAN
        else -> Language.ENGLISH
    }
// Revue me>>
