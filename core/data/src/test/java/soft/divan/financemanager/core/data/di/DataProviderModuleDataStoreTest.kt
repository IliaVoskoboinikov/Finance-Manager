package soft.divan.financemanager.core.data.di

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Robolectric-тест DataStore-провайдера: обращается к Context-делегату `currency_dataStore`,
 * который требует реального Context.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DataProviderModuleDataStoreTest {

    @Test
    fun `provideDataStore returns a preferences datastore`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        assertThat(DataProviderModule.provideDataStore(context)).isNotNull()
    }
}
