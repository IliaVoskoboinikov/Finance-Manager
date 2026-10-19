package soft.divan.financemanager.core.data.vault

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import soft.divan.financemanager.core.data.di.LocalDataModule
import soft.divan.financemanager.core.data.mapper.toDomain
import soft.divan.financemanager.core.data.mapper.toKeyLevel
import soft.divan.financemanager.core.database.holder.DatabaseFactory
import soft.divan.financemanager.core.database.holder.DatabaseFiles
import soft.divan.financemanager.core.database.holder.DatabaseState
import soft.divan.financemanager.core.database.holder.RoomDatabaseHolder
import soft.divan.financemanager.core.domain.model.DataProtectionLevel
import soft.divan.financemanager.core.security.keyset.KeyLevel

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LocalDataWiringTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `cleanup on sign-out is a crypto-shred`() = runTest {
        val vault = mockk<LocalDataVault>(relaxUnitFun = true)

        CryptoShredCleanupManager(vault).clearUserData()

        coVerify(exactly = 1) { vault.wipe() }
    }

    @Test
    fun `levels map both ways`() {
        DataProtectionLevel.entries.forEach { level ->
            assertThat(level.toKeyLevel().toDomain()).isEqualTo(level)
        }
        assertThat(KeyLevel.OPEN.toDomain()).isEqualTo(DataProtectionLevel.NONE)
        assertThat(KeyLevel.DEVICE.toDomain()).isEqualTo(DataProtectionLevel.DEVICE)
        assertThat(KeyLevel.PIN.toDomain()).isEqualTo(DataProtectionLevel.PIN)
    }

    @Test
    fun `holder is provided closed until the vault opens it`() {
        val holder = LocalDataModule.provideDatabaseHolder(
            factory = mockk<DatabaseFactory>(),
            files = DatabaseFiles(context),
            dispatcher = StandardTestDispatcher()
        )

        assertThat(holder).isInstanceOf(RoomDatabaseHolder::class.java)
        assertThat(holder.state.value).isEqualTo(DatabaseState.CLOSED)
    }
}
