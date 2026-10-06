package soft.divan.financemanager.core.data.testing

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import soft.divan.financemanager.core.data.vault.BiometricVault
import soft.divan.financemanager.core.data.vault.LocalDataVault
import soft.divan.financemanager.core.data.vault.PinUnlocker
import soft.divan.financemanager.core.data.vault.ProtectionLevelSwitcher
import soft.divan.financemanager.core.data.vault.VaultCore
import soft.divan.financemanager.core.database.db.FinanceManagerDatabase
import soft.divan.financemanager.core.database.db.withAppDefaults
import soft.divan.financemanager.core.database.holder.DatabaseFactory
import soft.divan.financemanager.core.database.holder.DatabaseFiles
import soft.divan.financemanager.core.database.holder.OpenedDatabase
import soft.divan.financemanager.core.database.holder.RoomDatabaseHolder
import soft.divan.financemanager.core.security.crypto.PinKeyDerivation
import soft.divan.financemanager.core.security.keyset.BiometricEnvelope
import soft.divan.financemanager.core.security.keyset.DekEnvelope
import soft.divan.financemanager.core.security.keyset.PinAttemptPolicy
import soft.divan.financemanager.core.security.keyset.impl.DataStoreKeysetStore
import java.io.File
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Хранилище ключей в сборе на настоящих частях: DataStore в файле, Room на обычном SQLite
 * (SQLCipher в JVM не загрузится), программный Keystore.
 *
 * [restart] имитирует новый процесс: тот же диск (набор ключей, файл базы, Keystore), но новые
 * холдер и хранилище — как после перезапуска приложения.
 */
class VaultHarness(
    private val context: Context,
    keysetFile: File,
    private val dispatcher: CoroutineDispatcher
) {

    val keys = FakeKeystoreKeys()
    val clock = MutableClock(Instant.parse("2026-09-01T12:00:00Z"))
    val files = DatabaseFiles(context)
    val envelope = DekEnvelope(keys, PinKeyDerivation(), pinIterations = PIN_ITERATIONS)

    private val dataStoreScope = CoroutineScope(SupervisorJob())

    /** Файл набора ключей как есть — чтобы тесты могли его испортить. */
    val dataStore: DataStore<Preferences> =
        PreferenceDataStoreFactory.create(scope = dataStoreScope, produceFile = { keysetFile })
    val store = DataStoreKeysetStore(dataStore)

    /** Ключи, которыми открывалась база, — копии на момент открытия. */
    val openedWith = mutableListOf<ByteArray>()

    /** Следующее открытие базы упадёт с этим исключением — как на испорченном файле. */
    var failNextOpen: RuntimeException? = null

    lateinit var core: VaultCore
        private set
    lateinit var holder: RoomDatabaseHolder
        private set
    lateinit var vault: LocalDataVault
        private set
    lateinit var unlocker: PinUnlocker
        private set
    lateinit var switcher: ProtectionLevelSwitcher
        private set
    lateinit var biometrics: BiometricVault
        private set

    init {
        assemble()
    }

    /** Новый «процесс» на том же диске; прежняя база должна быть уже закрыта. */
    fun restart() {
        assemble()
    }

    /** Закрывает базу и освобождает DataStore. */
    suspend fun release() {
        holder.close()
        files.delete()
        dataStoreScope.cancel()
    }

    private fun assemble() {
        val factory = DatabaseFactory { key ->
            failNextOpen?.let { error ->
                failNextOpen = null
                key.fill(0)
                throw error
            }
            openedWith += key.copyOf()
            val database = Room.databaseBuilder(
                context,
                FinanceManagerDatabase::class.java,
                DatabaseFiles.NAME
            ).withAppDefaults().build()
            database.openHelper.writableDatabase
            OpenedDatabase(database) { key.fill(0) }
        }
        holder = RoomDatabaseHolder(factory, files, dispatcher)
        core = VaultCore(holder, store, keys, envelope, dispatcher)
        vault = LocalDataVault(core, envelope, files)
        unlocker = PinUnlocker(core, envelope, PinAttemptPolicy(clock))
        switcher = ProtectionLevelSwitcher(core, envelope)
        biometrics = BiometricVault(core, envelope, BiometricEnvelope(keys))
    }

    companion object {
        /** Итераций PBKDF2 мало: проверяется логика, а не стойкость. */
        const val PIN_ITERATIONS = 1_000
    }
}

/** Часы, которые тест двигает сам. */
class MutableClock(var now: Instant) : Clock() {
    override fun getZone(): ZoneId = ZoneOffset.UTC
    override fun withZone(zone: ZoneId?): Clock = this
    override fun instant(): Instant = now
}
