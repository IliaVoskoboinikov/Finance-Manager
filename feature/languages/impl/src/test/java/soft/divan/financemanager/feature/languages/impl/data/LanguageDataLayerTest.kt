package soft.divan.financemanager.feature.languages.impl.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.feature.languages.impl.data.repository.LanguageRepositoryImpl
import soft.divan.financemanager.feature.languages.impl.data.source.LanguagesLocalSource
import soft.divan.financemanager.feature.languages.impl.data.source.impl.KEY_LANGUAGE
import soft.divan.financemanager.feature.languages.impl.data.source.impl.LanguageLocalSourceImpl
import soft.divan.financemanager.feature.languages.impl.domain.model.Language

class LanguageDataLayerTest {

    /* ---------- LanguageRepositoryImpl ---------- */

    private val localSource = mockk<LanguagesLocalSource>(relaxUnitFun = true)
    private val repository = LanguageRepositoryImpl(localSource)

    @Test
    fun `observeLanguage maps stored tag to domain`() = runTest {
        every { localSource.observe() } returns flowOf("ru")

        assertThat(repository.observeLanguage().first()).isEqualTo(Language.RUSSIAN)
    }

    @Test
    fun `observeLanguage passes null through for missing value`() = runTest {
        every { localSource.observe() } returns flowOf(null)

        assertThat(repository.observeLanguage().first()).isNull()
    }

    @Test
    fun `setLanguage stores language tag`() = runTest {
        repository.setLanguage(Language.ENGLISH)

        coVerify(exactly = 1) { localSource.save("en") }
    }

    /* ---------- LanguageLocalSourceImpl ---------- */

    private val dataStore = mockk<DataStore<Preferences>>()
    private val dataSource = LanguageLocalSourceImpl(dataStore)

    @Test
    fun `observe returns stored language code`() = runTest {
        every { dataStore.data } returns flowOf(preferencesOf(KEY_LANGUAGE to "ru"))

        assertThat(dataSource.observe().first()).isEqualTo("ru")
    }

    @Test
    fun `observe returns null when nothing stored`() = runTest {
        every { dataStore.data } returns flowOf(emptyPreferences())

        assertThat(dataSource.observe().first()).isNull()
    }

    @Test
    fun `save stores code under language key`() = runTest {
        val transform = slot<suspend (Preferences) -> Preferences>()
        coEvery { dataStore.updateData(capture(transform)) } coAnswers {
            transform.captured(emptyPreferences())
        }

        dataSource.save("en")

        assertThat(transform.captured(emptyPreferences())[KEY_LANGUAGE]).isEqualTo("en")
    }
}
