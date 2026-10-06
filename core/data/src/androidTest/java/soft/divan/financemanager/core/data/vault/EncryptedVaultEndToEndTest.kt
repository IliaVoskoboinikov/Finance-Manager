package soft.divan.financemanager.core.data.vault

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import soft.divan.financemanager.core.database.entity.CurrencyEntity
import soft.divan.financemanager.core.database.holder.DatabaseFiles
import soft.divan.financemanager.core.database.holder.DatabaseState
import soft.divan.financemanager.core.database.holder.RoomDatabaseHolder
import soft.divan.financemanager.core.database.holder.SqlCipherDatabaseFactory
import soft.divan.financemanager.core.domain.model.LocalDataState
import soft.divan.financemanager.core.domain.model.PinUnlockResult
import soft.divan.financemanager.core.domain.model.ProtectionChangeResult
import soft.divan.financemanager.core.security.crypto.PinKeyDerivation
import soft.divan.financemanager.core.security.keyset.DekEnvelope
import soft.divan.financemanager.core.security.keyset.KeyLevel
import soft.divan.financemanager.core.security.keyset.PinAttemptPolicy
import soft.divan.financemanager.core.security.keyset.impl.DataStoreKeysetStore
import soft.divan.financemanager.core.security.keystore.impl.AndroidKeystoreKeys
import java.io.File
import java.security.KeyStore
import java.time.Clock
import java.util.concurrent.Executors

/**
 * Хранилище ключей целиком на устройстве: SQLCipher, Android Keystore, DataStore — ничего не
 * подменено. Проверяет то, что юнит-тесты проверить не могут: ключ из Keystore действительно
 * открывает зашифрованный файл после «перезапуска», а уровни защиты меняются без перешифровки
 * данных.
 */
@RunWith(AndroidJUnit4::class)
class EncryptedVaultEndToEndTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val keys = AndroidKeystoreKeys(keyStore)
    private val files = DatabaseFiles(context)
    private val envelope = DekEnvelope(keys, PinKeyDerivation(), pinIterations = TEST_ITERATIONS)
    private val io = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
    private val dataStoreScope = CoroutineScope(SupervisorJob())
    private val store = DataStoreKeysetStore(
        PreferenceDataStoreFactory.create(scope = dataStoreScope) {
            File(context.noBackupFilesDir, "vault-test/keyset.preferences_pb")
        }
    )

    private lateinit var holder: RoomDatabaseHolder
    private lateinit var vault: LocalDataVault
    private lateinit var unlocker: PinUnlocker
    private lateinit var switcher: ProtectionLevelSwitcher

    @Before
    fun setUp() = runBlocking {
        files.delete()
        store.clear()
        keys.aliases(DekEnvelope.KEY_PREFIX).forEach(keys::delete)
        startProcess()
    }

    @After
    fun tearDown() = runBlocking {
        holder.close()
        files.delete()
        store.clear()
        keys.aliases(DekEnvelope.KEY_PREFIX).forEach(keys::delete)
        dataStoreScope.cancel()
        io.close()
    }

    @Test
    fun deviceKeyReopensTheEncryptedDatabaseAfterRestart() = runBlocking {
        vault.initialize()
        assertEquals(LocalDataState.Open, vault.state.value)
        assertFalse(files.isPlaintext())
        holder.withDatabase { it.currencyDao().insertCurrencies(listOf(CurrencyEntity("rub", "Рубль"))) }

        holder.close()
        startProcess()
        vault.initialize()

        assertEquals(LocalDataState.Open, vault.state.value)
        assertNotNull(holder.withDatabase { it.currencyDao().getCurrencyById("rub") })
    }

    @Test
    fun pinLevelLocksAndUnlocksTheSameData() = runBlocking {
        vault.initialize()
        holder.withDatabase { it.currencyDao().insertCurrencies(listOf(CurrencyEntity("rub", "Рубль"))) }

        assertEquals(
            ProtectionChangeResult.Changed,
            switcher.changeLevel(KeyLevel.PIN, PIN.toCharArray())
        )
        vault.lock()
        assertEquals(LocalDataState.Locked(biometricEnabled = false), vault.state.value)
        assertEquals(DatabaseState.CLOSED, holder.state.value)

        val wrong = unlocker.unlockWithPin(WRONG.toCharArray()) as VaultPinResult.Done
        assertTrue(wrong.result is PinUnlockResult.WrongPin)
        assertEquals(
            VaultPinResult.Done(PinUnlockResult.Success),
            unlocker.unlockWithPin(PIN.toCharArray())
        )

        assertEquals(LocalDataState.Open, vault.state.value)
        assertNotNull(holder.withDatabase { it.currencyDao().getCurrencyById("rub") })
    }

    @Test
    fun openLevelAndBackKeepTheSameKey() = runBlocking {
        vault.initialize()
        holder.withDatabase {
            it.currencyDao().insertCurrencies(
                listOf(CurrencyEntity("usd", "Доллар"))
            )
        }
        val deviceKeyset = store.read()!!
        val dek = envelope.openWithoutUser(deviceKeyset)

        assertEquals(ProtectionChangeResult.Changed, switcher.changeLevel(KeyLevel.OPEN, null))
        // Уровень сменился, ключ базы — тот же: файл не перешифровывался
        assertArrayEquals(dek, store.read()!!.openKey)
        // Ключ Keystore прежнего уровня удалён сразу после записи нового набора
        assertFalse(keys.aliases(DekEnvelope.KEY_PREFIX).contains(deviceKeyset.sealed!!.alias))
        assertEquals(ProtectionChangeResult.Changed, switcher.changeLevel(KeyLevel.DEVICE, null))

        holder.close()
        startProcess()
        vault.initialize()
        assertNotNull(holder.withDatabase { it.currencyDao().getCurrencyById("usd") })
    }

    @Test
    fun wipeLeavesAnEmptySeededDatabaseUnderANewKey() = runBlocking {
        vault.initialize()
        holder.withDatabase { it.currencyDao().insertCurrencies(listOf(CurrencyEntity("rub", "Рубль"))) }

        vault.wipe()

        assertEquals(LocalDataState.Open, vault.state.value)
        assertNull(holder.withDatabase { it.currencyDao().getCurrencyById("rub") })
        assertEquals(SEEDED_CATEGORIES, holder.withDatabase { it.categoryDao().getAll().first() }.size)
    }

    /** Новые холдер и хранилище на том же диске и том же Keystore — как после перезапуска. */
    private fun startProcess() {
        holder = RoomDatabaseHolder(SqlCipherDatabaseFactory(context), files, io)
        val core = VaultCore(holder, store, keys, envelope, io)
        vault = LocalDataVault(core, envelope, files)
        unlocker = PinUnlocker(core, envelope, PinAttemptPolicy(Clock.systemUTC()))
        switcher = ProtectionLevelSwitcher(core, envelope)
    }

    private companion object {
        const val PIN = "2468"
        const val WRONG = "1357"
        const val TEST_ITERATIONS = 10_000
        const val SEEDED_CATEGORIES = 24
    }
}
