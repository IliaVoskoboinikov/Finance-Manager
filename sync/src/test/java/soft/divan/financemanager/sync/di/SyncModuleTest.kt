package soft.divan.financemanager.sync.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.sync.data.source.impl.SyncLocalSourceImpl

class SyncModuleTest {

    @Test
    fun `providePreferences builds datastore-backed source`() {
        val source = SyncModule.providePreferences(mockk<DataStore<Preferences>>())

        assertThat(source).isInstanceOf(SyncLocalSourceImpl::class.java)
    }
}
