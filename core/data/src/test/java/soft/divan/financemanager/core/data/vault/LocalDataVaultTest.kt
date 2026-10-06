package soft.divan.financemanager.core.data.vault

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteFullException
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import soft.divan.financemanager.core.data.testing.VaultHarness
import soft.divan.financemanager.core.database.entity.CurrencyEntity
import soft.divan.financemanager.core.database.holder.DatabaseState
import soft.divan.financemanager.core.domain.model.LocalDataState
import soft.divan.financemanager.core.domain.model.PinLockout
import soft.divan.financemanager.core.domain.model.PinUnlockResult
import soft.divan.financemanager.core.domain.model.ProtectionChangeResult
import soft.divan.financemanager.core.security.keyset.DekEnvelope
import soft.divan.financemanager.core.security.keyset.KeyLevel
import soft.divan.financemanager.core.security.keyset.Keyset
import soft.divan.financemanager.core.security.keyset.KeysetStore
import soft.divan.financemanager.core.security.keyset.PinAttemptPolicy
import soft.divan.financemanager.core.security.keyset.PinAttempts
import soft.divan.financemanager.core.security.keystore.KeyProtection
import java.io.File
import java.io.IOException
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LocalDataVaultTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val dispatcher = StandardTestDispatcher()
    private val harness by lazy {
        VaultHarness(context, File(folder.root, "keyset.preferences_pb"), dispatcher)
    }
    private val vault get() = harness.vault
    private val unlocker get() = harness.unlocker

    @Test
    fun `first run creates a device-level key and a seeded database`() = vaultTest {
        vault.initialize()

        val keyset = harness.store.read()!!
        assertThat(vault.state.value).isEqualTo(LocalDataState.Open)
        assertThat(keyset.level).isEqualTo(KeyLevel.DEVICE)
        assertThat(harness.keys.aliases(DekEnvelope.KEY_PREFIX)).containsExactly(keyset.sealed!!.alias)
        assertThat(categories()).hasSize(SEEDED_CATEGORIES)
    }

    @Test
    fun `restart opens the same database with the same key`() = vaultTest {
        vault.initialize()
        harness.holder.withDatabase {
            it.currencyDao().insertCurrencies(
                listOf(CurrencyEntity("rub", "Рубль"))
            )
        }
        harness.holder.close()

        harness.restart()
        vault.initialize()

        assertThat(vault.state.value).isEqualTo(LocalDataState.Open)
        assertThat(harness.openedWith).hasSize(2)
        assertThat(harness.openedWith[1]).isEqualTo(harness.openedWith[0])
        assertThat(harness.holder.withDatabase { it.currencyDao().getCurrencyById("rub") }).isNotNull()
    }

    @Test
    fun `initialize is a no-op once data is open`() = vaultTest {
        vault.initialize()

        vault.initialize()

        assertThat(harness.openedWith).hasSize(1)
    }

    @Test
    fun `keystore keys left without a keyset are removed at start`() = vaultTest {
        vault.initialize()
        val orphan = harness.keys.create(DekEnvelope.DEVICE_PREFIX, KeyProtection.ALWAYS_AVAILABLE)
        val foreign = harness.keys.create("pin_alias", KeyProtection.ALWAYS_AVAILABLE)
        harness.holder.close()

        harness.restart()
        vault.initialize()

        assertThat(harness.keys.keys).doesNotContainKey(orphan.alias)
        // Чужие ключи (токены, хеш PIN) уборка не трогает
        assertThat(harness.keys.keys).containsKey(foreign.alias)
    }

    @Test
    fun `database without a keyset means the key is lost, not a first run`() = vaultTest {
        vault.initialize()
        harness.holder.close()
        harness.store.clear()
        // В тестах база — обычный SQLite; на устройстве файл зашифрован и выглядит как шум
        harness.files.file.writeBytes(ByteArray(ENCRYPTED_FILE_SIZE) { (it * 31).toByte() })

        harness.restart()
        vault.initialize()

        assertThat(vault.state.value).isEqualTo(LocalDataState.KeyLost)
        assertThat(harness.files.exists()).isTrue()
    }

    @Test
    fun `lost keystore key leads to recovery, wipe starts over`() = vaultTest {
        vault.initialize()
        harness.holder.close()
        harness.keys.delete(harness.store.read()!!.sealed!!.alias)

        harness.restart()
        vault.initialize()
        assertThat(vault.state.value).isEqualTo(LocalDataState.KeyLost)

        vault.wipe()

        assertThat(vault.state.value).isEqualTo(LocalDataState.Open)
        assertThat(harness.store.read()!!.level).isEqualTo(KeyLevel.DEVICE)
        assertThat(categories()).hasSize(SEEDED_CATEGORIES)
    }

    @Test
    fun `corrupted keyset leads to recovery`() = vaultTest {
        vault.initialize()
        harness.holder.close()
        harness.store.write(harness.store.read()!!)
        rawKeyset { it[stringPreferencesKey("sealed_blob")] = "not base64 at all %%%" }

        harness.restart()
        vault.initialize()

        assertThat(vault.state.value).isEqualTo(LocalDataState.KeyLost)
    }

    @Test
    fun `retry after key loss re-reads the keys`() = vaultTest {
        vault.initialize()
        harness.holder.close()
        val keyset = harness.store.read()!!
        rawKeyset { it[stringPreferencesKey("level")] = "BROKEN" }
        harness.restart()
        vault.initialize()
        assertThat(vault.state.value).isEqualTo(LocalDataState.KeyLost)

        harness.store.write(keyset)
        vault.initialize()

        assertThat(vault.state.value).isEqualTo(LocalDataState.Open)
    }

    @Test
    fun `keystore refusing to create the first key is reported`() = vaultTest {
        harness.keys.failCreate = true

        vault.initialize()

        assertThat(vault.state.value).isEqualTo(LocalDataState.KeyLost)
        assertThat(harness.store.read()).isNull()
    }

    @Test
    fun `plaintext database from before encryption is replaced silently`() = vaultTest {
        harness.files.file.parentFile!!.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(harness.files.file, null).use {
            it.execSQL("CREATE TABLE legacy (id INTEGER)")
        }

        vault.initialize()

        val legacyTables = harness.holder.withDatabase { db ->
            db.openHelper.readableDatabase
                .query("SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'legacy'")
                .use { it.count }
        }
        assertThat(vault.state.value).isEqualTo(LocalDataState.Open)
        assertThat(legacyTables).isZero()
        assertThat(categories()).hasSize(SEEDED_CATEGORIES)
    }

    @Test
    fun `plaintext check applies only to files from before the keys existed`() = vaultTest {
        vault.initialize()
        harness.holder.withDatabase {
            it.currencyDao().insertCurrencies(
                listOf(CurrencyEntity("rub", "Рубль"))
            )
        }
        harness.holder.close()

        // Набор ключей есть — значит, файл создан уже с шифрованием и удалять его нельзя
        harness.restart()
        vault.initialize()

        assertThat(harness.holder.withDatabase { it.currencyDao().getCurrencyById("rub") }).isNotNull()
    }

    @Test
    fun `pin level starts locked`() = vaultTest {
        enablePinLevel()
        harness.holder.close()

        harness.restart()
        vault.initialize()

        assertThat(vault.state.value).isEqualTo(LocalDataState.Locked(biometricEnabled = false))
        assertThat(harness.holder.state.value).isEqualTo(DatabaseState.CLOSED)
    }

    @Test
    fun `right pin opens the database and clears the failures`() = vaultTest {
        lockedAtPinLevel()
        unlocker.unlockWithPin("0000".toCharArray())

        val result = unlocker.unlockWithPin(PIN.toCharArray())

        assertThat(result).isEqualTo(VaultPinResult.Done(PinUnlockResult.Success))
        assertThat(vault.state.value).isEqualTo(LocalDataState.Open)
        assertThat(harness.store.readAttempts()).isEqualTo(PinAttempts())
        assertThat(harness.openedWith.last()).isEqualTo(harness.openedWith.first())
    }

    @Test
    fun `pin array is wiped after the check`() = vaultTest {
        lockedAtPinLevel()
        val pin = PIN.toCharArray()

        unlocker.unlockWithPin(pin)

        assertThat(pin).containsOnly(Char.MIN_VALUE)
    }

    @Test
    fun `wrong pins pause and the tenth one exhausts the attempts`() = vaultTest {
        lockedAtPinLevel()

        repeat(PinAttemptPolicy.FREE_FAILURES) { index ->
            val result = unlocker.unlockWithPin(WRONG.toCharArray())
            assertThat(result).isEqualTo(
                VaultPinResult.Done(PinUnlockResult.WrongPin(PinLockout(attemptsLeft = 9 - index)))
            )
        }
        PinAttemptPolicy.LOCKOUTS.forEach { lockout ->
            val result = unlocker.unlockWithPin(WRONG.toCharArray()) as VaultPinResult.Done
            val wrong = result.result as PinUnlockResult.WrongPin
            assertThat(wrong.lockout.lockedUntil).isEqualTo(harness.clock.now + lockout)

            // Во время паузы PIN не проверяется и попытка не тратится
            val refused = unlocker.unlockWithPin(PIN.toCharArray()) as VaultPinResult.Done
            assertThat(refused.result).isInstanceOf(PinUnlockResult.LockedOut::class.java)
            harness.clock.now += lockout
        }

        assertThat(unlocker.unlockWithPin(WRONG.toCharArray())).isEqualTo(VaultPinResult.Exhausted)
        // Исчерпанные попытки не восстанавливаются даже верным PIN
        assertThat(unlocker.unlockWithPin(PIN.toCharArray())).isEqualTo(VaultPinResult.Exhausted)
        assertThat(vault.state.value).isInstanceOf(LocalDataState.Locked::class.java)
    }

    @Test
    fun `attempt is recorded before the pin is checked`() = vaultTest {
        lockedAtPinLevel()
        var failuresDuringCheck = -1
        // Ключ Keystore берётся уже внутри проверки: к этому моменту ошибка обязана быть записана,
        // иначе процесс, убитый посреди проверки, дал бы попытку бесплатно
        harness.keys.onFind = {
            if (failuresDuringCheck < 0) {
                failuresDuringCheck = runBlocking { harness.store.readAttempts().failures }
            }
        }

        unlocker.unlockWithPin(PIN.toCharArray())

        assertThat(failuresDuringCheck).isEqualTo(1)
        assertThat(harness.store.readAttempts()).isEqualTo(PinAttempts())
    }

    @Test
    fun `database that fails to open after the right pin leads to recovery`() = vaultTest {
        lockedAtPinLevel()
        unlocker.unlockWithPin(WRONG.toCharArray())
        harness.failNextOpen = SQLiteFullException("database or disk is full")

        val result = unlocker.unlockWithPin(PIN.toCharArray())

        // Не падение приложения, а экран восстановления; PIN был верный — ошибки сброшены
        assertThat(result).isEqualTo(VaultPinResult.Done(PinUnlockResult.KeyLost))
        assertThat(vault.state.value).isEqualTo(LocalDataState.KeyLost)
        assertThat(harness.store.readAttempts()).isEqualTo(PinAttempts())
    }

    @Test
    fun `keyset unreadable from disk leads to recovery instead of hanging`() = vaultTest {
        val failing = object : KeysetStore by harness.store {
            override suspend fun read(): Keyset? = throw IOException("I/O error")
        }
        val core = VaultCore(harness.holder, failing, harness.keys, harness.envelope, dispatcher)
        val vault = LocalDataVault(core, harness.envelope, harness.files)

        vault.initialize()

        assertThat(vault.state.value).isEqualTo(LocalDataState.KeyLost)
    }

    @Test
    fun `lost outer key does not cost an attempt`() = vaultTest {
        lockedAtPinLevel()
        unlocker.unlockWithPin(WRONG.toCharArray())
        val before = harness.store.readAttempts()
        harness.keys.delete(harness.store.read()!!.sealed!!.alias)

        val result = unlocker.unlockWithPin(PIN.toCharArray())

        assertThat(result).isEqualTo(VaultPinResult.Done(PinUnlockResult.KeyLost))
        // PIN не проверялся: повтор после временного сбоя Keystore не приближает стирание
        assertThat(harness.store.readAttempts()).isEqualTo(before)
        assertThat(vault.state.value).isEqualTo(LocalDataState.KeyLost)
    }

    @Test
    fun `lockout survives moving the clock back`() = vaultTest {
        lockedAtPinLevel()
        repeat(PinAttemptPolicy.FREE_FAILURES + 1) { unlocker.unlockWithPin(WRONG.toCharArray()) }

        harness.clock.now -= Duration.ofDays(1)

        val result = unlocker.unlockWithPin(PIN.toCharArray()) as VaultPinResult.Done
        assertThat(result.result).isInstanceOf(PinUnlockResult.LockedOut::class.java)
    }

    @Test
    fun `pin is not required below the pin level`() = vaultTest {
        vault.initialize()

        assertThat(unlocker.unlockWithPin(PIN.toCharArray()))
            .isEqualTo(VaultPinResult.Done(PinUnlockResult.NotRequired))
    }

    @Test
    fun `unreadable keyset during unlock is reported as key loss`() = vaultTest {
        lockedAtPinLevel()
        rawKeyset { it[stringPreferencesKey("pin_salt")] = "%%%" }

        assertThat(unlocker.unlockWithPin(PIN.toCharArray()))
            .isEqualTo(VaultPinResult.Done(PinUnlockResult.KeyLost))
    }

    @Test
    fun `pin lockout reports what is left`() = vaultTest {
        lockedAtPinLevel()
        repeat(PinAttemptPolicy.FREE_FAILURES + 1) { unlocker.unlockWithPin(WRONG.toCharArray()) }

        val lockout = unlocker.pinLockout()

        assertThat(lockout.attemptsLeft).isEqualTo(5)
        assertThat(lockout.lockedUntil).isEqualTo(harness.clock.now + PinAttemptPolicy.LOCKOUTS.first())
    }

    @Test
    fun `lock closes a pin-level database and publishes the lock at once`() = vaultTest {
        enablePinLevel()

        vault.lock()

        assertThat(vault.state.value).isEqualTo(LocalDataState.Locked(biometricEnabled = false))
        assertThat(harness.holder.state.value).isEqualTo(DatabaseState.CLOSED)
        assertThat(harness.openedWith.single()).isNotEmpty()
    }

    @Test
    fun `lock does nothing below the pin level`() = vaultTest {
        vault.initialize()

        vault.lock()

        assertThat(vault.state.value).isEqualTo(LocalDataState.Open)
        assertThat(harness.holder.state.value).isEqualTo(DatabaseState.OPEN)
    }

    @Test
    fun `wipe destroys keys and data and starts over at the device level`() = vaultTest {
        enablePinLevel()
        harness.holder.withDatabase {
            it.currencyDao().insertCurrencies(
                listOf(CurrencyEntity("rub", "Рубль"))
            )
        }
        harness.store.writeAttempts(PinAttempts(failures = 3))
        val before = harness.keys.aliases(DekEnvelope.KEY_PREFIX)

        vault.wipe()

        val keyset = harness.store.read()!!
        assertThat(vault.state.value).isEqualTo(LocalDataState.Open)
        assertThat(keyset.level).isEqualTo(KeyLevel.DEVICE)
        assertThat(harness.keys.aliases(DekEnvelope.KEY_PREFIX)).doesNotContainAnyElementsOf(before)
        assertThat(harness.store.readAttempts()).isEqualTo(PinAttempts())
        assertThat(harness.holder.withDatabase { it.currencyDao().getCurrencyById("rub") }).isNull()
        assertThat(categories()).hasSize(SEEDED_CATEGORIES)
        // Новый ключ базы — старые данные не расшифровать и при сохранившемся файле
        assertThat(harness.openedWith.last()).isNotEqualTo(harness.openedWith.first())
    }

    private suspend fun enablePinLevel() {
        vault.initialize()
        val result = harness.switcher.changeLevel(KeyLevel.PIN, PIN.toCharArray())
        assertThat(result).isEqualTo(ProtectionChangeResult.Changed)
    }

    private suspend fun lockedAtPinLevel() {
        enablePinLevel()
        vault.lock()
    }

    private suspend fun categories() = harness.holder.withDatabase { it.categoryDao().getAll().first() }

    private suspend fun rawKeyset(block: (MutablePreferences) -> Unit) {
        harness.dataStore.edit(block)
    }

    private fun vaultTest(body: suspend TestScope.() -> Unit) = runTest(dispatcher) {
        try {
            body()
        } finally {
            harness.release()
        }
    }

    private companion object {
        const val PIN = "2468"
        const val WRONG = "1357"

        /** Справочник категорий, засеваемый в каждую новую базу. */
        const val SEEDED_CATEGORIES = 24

        const val ENCRYPTED_FILE_SIZE = 4096
    }
}
