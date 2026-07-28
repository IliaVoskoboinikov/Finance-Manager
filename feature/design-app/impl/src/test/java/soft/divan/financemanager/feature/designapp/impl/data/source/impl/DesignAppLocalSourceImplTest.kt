package soft.divan.financemanager.feature.designapp.impl.data.source.impl

import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.Color
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.preferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import soft.divan.financemanager.feature.designapp.impl.domain.model.ThemeMode
import soft.divan.financemanager.uikit.theme.AccentColor

class DesignAppLocalSourceImplTest {

    private val dataStore = mockk<DataStore<Preferences>>()
    private val localSource = DesignAppLocalSourceImpl(dataStore)

    private val themeKey = stringPreferencesKey("app_theme_mode")
    private val accentKey = stringPreferencesKey("app_accent_color")
    private val customKey = stringPreferencesKey("app_custom_accent_hex")

    @Before
    fun setUp() {
        // toColorInt() → android.graphics.Color.parseColor — Android-фреймворк, в JVM мокаем
        mockkStatic(AndroidColor::class)
        every { AndroidColor.parseColor("#FF123456") } returns 0xFF123456.toInt()
    }

    @After
    fun tearDown() {
        unmockkStatic(AndroidColor::class)
    }

    @Test
    fun `getThemeMode returns stored mode`() = runTest {
        every { dataStore.data } returns flowOf(preferencesOf(themeKey to "DARK"))

        assertThat(localSource.getThemeMode().first()).isEqualTo(ThemeMode.DARK)
    }

    @Test
    fun `getThemeMode defaults to SYSTEM`() = runTest {
        every { dataStore.data } returns flowOf(emptyPreferences())

        assertThat(localSource.getThemeMode().first()).isEqualTo(ThemeMode.SYSTEM)
    }

    @Test
    fun `getThemeMode falls back to SYSTEM for unknown value`() = runTest {
        every { dataStore.data } returns flowOf(preferencesOf(themeKey to "NEON"))

        assertThat(localSource.getThemeMode().first()).isEqualTo(ThemeMode.SYSTEM)
    }

    @Test
    fun `setThemeMode stores enum name`() = runTest {
        val transform = slot<suspend (Preferences) -> Preferences>()
        coEvery { dataStore.updateData(capture(transform)) } coAnswers {
            transform.captured(emptyPreferences())
        }

        localSource.setThemeMode(ThemeMode.LIGHT)

        assertThat(transform.captured(emptyPreferences())[themeKey]).isEqualTo("LIGHT")
    }

    @Test
    fun `getAccentColor returns stored accent`() = runTest {
        every { dataStore.data } returns flowOf(preferencesOf(accentKey to "PURPLE"))

        assertThat(localSource.getAccentColor().first()).isEqualTo(AccentColor.PURPLE)
    }

    @Test
    fun `getAccentColor defaults to MINT`() = runTest {
        every { dataStore.data } returns flowOf(emptyPreferences())

        assertThat(localSource.getAccentColor().first()).isEqualTo(AccentColor.MINT)
    }

    @Test
    fun `setAccentColor stores enum name`() = runTest {
        val transform = slot<suspend (Preferences) -> Preferences>()
        coEvery { dataStore.updateData(capture(transform)) } coAnswers {
            transform.captured(emptyPreferences())
        }

        localSource.setAccentColor(AccentColor.BLUE)

        assertThat(transform.captured(emptyPreferences())[accentKey]).isEqualTo("BLUE")
    }

    @Test
    fun `getCustomAccentColor parses stored hex`() = runTest {
        every { dataStore.data } returns flowOf(preferencesOf(customKey to "#FF123456"))

        assertThat(localSource.getCustomAccentColor().first()).isEqualTo(Color(0xFF123456))
    }

    @Test
    fun `getCustomAccentColor returns null when nothing stored`() = runTest {
        every { dataStore.data } returns flowOf(emptyPreferences())

        assertThat(localSource.getCustomAccentColor().first()).isNull()
    }

    @Test
    fun `getCustomAccentColor returns null for malformed hex`() = runTest {
        every { AndroidColor.parseColor("garbage") } throws
            IllegalArgumentException("Unknown color")
        every { dataStore.data } returns flowOf(preferencesOf(customKey to "garbage"))

        assertThat(localSource.getCustomAccentColor().first()).isNull()
    }

    @Test
    fun `setCustomAccentColor stores color as hex string`() = runTest {
        val transform = slot<suspend (Preferences) -> Preferences>()
        coEvery { dataStore.updateData(capture(transform)) } coAnswers {
            transform.captured(emptyPreferences())
        }

        localSource.setCustomAccentColor(Color(0xFF123456))

        assertThat(transform.captured(emptyPreferences())[customKey]).isEqualTo("#FF123456")
    }
}
