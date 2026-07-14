package soft.divan.financemanager.feature.languages.impl.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.test.core.app.ApplicationProvider
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import soft.divan.financemanager.feature.languages.impl.data.source.impl.LanguageLocalSourceImpl

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LanguagesModuleTest {

    @Test
    fun `provideLanguagesDataStore returns a preferences datastore`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        assertThat(LanguagesModule.provideLanguagesDataStore(context)).isNotNull()
    }

    @Test
    fun `provideLanguagesPreferences builds datastore-backed source`() {
        val source = LanguagesModule.provideLanguagesPreferences(mockk<DataStore<Preferences>>())

        assertThat(source).isInstanceOf(LanguageLocalSourceImpl::class.java)
    }
}
