package soft.divan.financemanager.core.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.mockk.mockk
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import soft.divan.financemanager.core.data.source.impl.CurrencyLocalDataSourceImpl
import soft.divan.financemanager.core.data.util.coroutne.impl.DefaultAppCoroutineContext
import soft.divan.financemanager.core.database.dao.CurrencyDao

class CoreDataDiModulesTest {

    /* ---------- CoroutineContextModule ---------- */

    @Test
    fun `provideAppCoroutineContext wires scope dispatcher and handler`() {
        val scope = CoroutineScope(SupervisorJob())
        val dispatcher = Dispatchers.Unconfined
        val handler = CoroutineExceptionHandler { _, _ -> }

        val context = CoroutineContextModule.provideAppCoroutineContext(
            scope = scope,
            dispatcher = dispatcher,
            exceptionHandler = handler
        )

        val impl = context as DefaultAppCoroutineContext
        assertThat(impl.scope).isSameAs(scope)
        assertThat(impl.dispatcher).isSameAs(dispatcher)
        assertThat(impl.exceptionHandler).isSameAs(handler)
    }

    /* ---------- DataProviderModule ---------- */

    @Test
    fun `provideCurrencyLocalDataSource builds datastore-backed implementation`() {
        val dataStore = mockk<DataStore<Preferences>>()
        val dao = mockk<CurrencyDao>()

        val source = DataProviderModule.provideCurrencyLocalDataSource(dataStore, dao)

        assertThat(source).isInstanceOf(CurrencyLocalDataSourceImpl::class.java)
    }

    @Test
    fun `api factories create retrofit services`() {
        val retrofit = Retrofit.Builder()
            .baseUrl("http://localhost/")
            .client(OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        assertThat(DataProviderModule.provideAccountApi(retrofit)).isNotNull()
        assertThat(DataProviderModule.provideTransactionApi(retrofit)).isNotNull()
        assertThat(DataProviderModule.provideCategoryApi(retrofit)).isNotNull()
    }
}
