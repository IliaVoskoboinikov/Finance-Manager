package soft.divan.financemanager.core.security.keyset.impl

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import soft.divan.financemanager.core.security.keyset.KeyLevel
import soft.divan.financemanager.core.security.keyset.Keyset
import soft.divan.financemanager.core.security.keyset.PinAttempts
import soft.divan.financemanager.core.security.keyset.SealedKey
import soft.divan.financemanager.core.security.keystore.KeyUnavailableException
import java.io.File

class DataStoreKeysetStoreTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val dataStoreScope = CoroutineScope(SupervisorJob())
    private val dataStore: DataStore<Preferences> by lazy {
        PreferenceDataStoreFactory.create(
            scope = dataStoreScope,
            produceFile = { File(folder.root, "keyset.preferences_pb") }
        )
    }
    private val store by lazy { DataStoreKeysetStore(dataStore) }

    @After
    fun tearDown() {
        dataStoreScope.cancel()
    }

    @Test
    fun `no keyset on first run`() = runTest {
        assertThat(store.read()).isNull()
        assertThat(store.observeLevel().first()).isNull()
    }

    @Test
    fun `open keyset round-trips`() = runTest {
        store.write(Keyset(level = KeyLevel.OPEN, openKey = byteArrayOf(1, 2, 3)))

        val read = store.read()!!
        assertThat(read.level).isEqualTo(KeyLevel.OPEN)
        assertThat(read.openKey).containsExactly(1, 2, 3)
        assertThat(read.sealed).isNull()
        assertThat(store.observeLevel().first()).isEqualTo(KeyLevel.OPEN)
    }

    @Test
    fun `pin keyset round-trips with biometric copy`() = runTest {
        store.write(pinKeyset())

        val read = store.read()!!
        assertThat(read.level).isEqualTo(KeyLevel.PIN)
        assertThat(read.sealed!!.alias).isEqualTo("db_kek_pin_1")
        assertThat(read.sealed!!.blob).containsExactly(4, 5, 6)
        assertThat(read.pinSalt).containsExactly(7, 8)
        assertThat(read.pinIterations).isEqualTo(310_000)
        assertThat(read.biometric!!.alias).isEqualTo("db_kek_bio_1")
        assertThat(read.biometric!!.blob).containsExactly(9)
    }

    @Test
    fun `biometric presence is observable`() = runTest {
        assertThat(store.observeBiometric().first()).isFalse()

        store.write(pinKeyset())
        assertThat(store.observeBiometric().first()).isTrue()

        store.write(pinKeyset().withoutBiometric())
        assertThat(store.observeBiometric().first()).isFalse()
    }

    @Test
    fun `write replaces the whole keyset`() = runTest {
        store.write(pinKeyset())

        store.write(
            Keyset(level = KeyLevel.DEVICE, sealed = SealedKey("db_kek_device_2", byteArrayOf(1)))
        )

        val read = store.read()!!
        assertThat(read.level).isEqualTo(KeyLevel.DEVICE)
        assertThat(read.pinSalt).isNull()
        assertThat(read.pinIterations).isZero()
        assertThat(read.biometric).isNull()
    }

    @Test
    fun `attempts live apart from the keyset`() = runTest {
        store.writeAttempts(PinAttempts(failures = 5, lockedUntilMillis = 100, lastFailureAtMillis = 50))

        store.write(pinKeyset())

        assertThat(store.readAttempts()).isEqualTo(PinAttempts(5, 100, 50))
    }

    @Test
    fun `attempts default to a clean record`() = runTest {
        assertThat(store.readAttempts()).isEqualTo(PinAttempts())
    }

    @Test
    fun `clear removes keyset and attempts`() = runTest {
        store.write(pinKeyset())
        store.writeAttempts(PinAttempts(failures = 3))

        store.clear()

        assertThat(store.read()).isNull()
        assertThat(store.readAttempts()).isEqualTo(PinAttempts())
    }

    @Test
    fun `corrupted value is reported instead of looking like a first run`() = runTest {
        dataStore.edit {
            it[stringPreferencesKey("level")] = "DEVICE"
            it[stringPreferencesKey("sealed_alias")] = "db_kek_device_1"
            it[stringPreferencesKey("sealed_blob")] = "%%% not base64 %%%"
        }

        val error = runCatching { store.read() }.exceptionOrNull()

        assertThat(error).isInstanceOf(KeyUnavailableException::class.java)
    }

    @Test
    fun `unknown level is reported as corrupted`() = runTest {
        dataStore.edit { it[stringPreferencesKey("level")] = "L7" }

        val error = runCatching { store.read() }.exceptionOrNull()

        assertThat(error).isInstanceOf(KeyUnavailableException::class.java)
        assertThat(store.observeLevel().first()).isNull()
    }

    @Test
    fun `sealed key needs both alias and blob`() = runTest {
        dataStore.edit {
            it[stringPreferencesKey("level")] = "DEVICE"
            it[stringPreferencesKey("sealed_alias")] = "db_kek_device_1"
        }

        assertThat(store.read()!!.sealed).isNull()
    }

    private fun pinKeyset() = Keyset(
        level = KeyLevel.PIN,
        sealed = SealedKey("db_kek_pin_1", byteArrayOf(4, 5, 6)),
        pinSalt = byteArrayOf(7, 8),
        pinIterations = 310_000,
        biometric = SealedKey("db_kek_bio_1", byteArrayOf(9))
    )
}
