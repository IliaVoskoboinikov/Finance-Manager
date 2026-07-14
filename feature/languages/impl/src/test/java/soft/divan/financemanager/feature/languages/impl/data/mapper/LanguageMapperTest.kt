package soft.divan.financemanager.feature.languages.impl.data.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.feature.languages.impl.domain.model.Language
import java.util.Locale

class LanguageMapperTest {

    @Test
    fun `string tag maps to matching language`() {
        assertThat("ru".toDomain()).isEqualTo(Language.RUSSIAN)
        assertThat("en".toDomain()).isEqualTo(Language.ENGLISH)
    }

    @Test
    fun `unknown or null string maps to null`() {
        assertThat("de".toDomain()).isNull()
        assertThat("".toDomain()).isNull()
        assertThat((null as String?).toDomain()).isNull()
    }

    @Test
    fun `russian locale maps to RUSSIAN`() {
        assertThat(Locale("ru").toDomain()).isEqualTo(Language.RUSSIAN)
        assertThat(Locale("ru", "RU").toDomain()).isEqualTo(Language.RUSSIAN)
    }

    @Test
    fun `non-russian locale falls back to ENGLISH`() {
        assertThat(Locale("en").toDomain()).isEqualTo(Language.ENGLISH)
        assertThat(Locale("de").toDomain()).isEqualTo(Language.ENGLISH)
    }

    @Test
    fun `null locale falls back to ENGLISH`() {
        assertThat((null as Locale?).toDomain()).isEqualTo(Language.ENGLISH)
    }
}
