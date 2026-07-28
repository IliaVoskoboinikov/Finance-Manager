package soft.divan.financemanager.feature.designapp.impl.di

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
import soft.divan.financemanager.feature.designapp.impl.data.source.impl.DesignAppLocalSourceImpl
import soft.divan.financemanager.feature.designapp.impl.precenter.model.DesignUiState
import soft.divan.financemanager.feature.designapp.impl.precenter.model.mockDesignUiStateError
import soft.divan.financemanager.feature.designapp.impl.precenter.model.mockDesignUiStateLoading
import soft.divan.financemanager.feature.designapp.impl.precenter.model.mockDesignUiStateSuccess

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DesignAppProviderModuleTest {

    @Test
    fun `provideDataStore returns a preferences datastore`() {
        val context = ApplicationProvider.getApplicationContext<Context>()

        assertThat(DesignAppProviderModule.provideDataStore(context)).isNotNull()
    }

    @Test
    fun `provideThemePreferences builds datastore-backed source`() {
        val source = DesignAppProviderModule.provideThemePreferences(
            mockk<DataStore<Preferences>>()
        )

        assertThat(source).isInstanceOf(DesignAppLocalSourceImpl::class.java)
    }

    @Test
    fun `preview mock data covers all ui states`() {
        assertThat(mockDesignUiStateLoading).isEqualTo(DesignUiState.Loading)
        assertThat(mockDesignUiStateSuccess).isInstanceOf(DesignUiState.Success::class.java)
        assertThat(mockDesignUiStateError).isInstanceOf(DesignUiState.Error::class.java)
    }
}
