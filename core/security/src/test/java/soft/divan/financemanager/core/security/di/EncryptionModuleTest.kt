package soft.divan.financemanager.core.security.di

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class EncryptionModuleTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun `keyset lives in the no-backup directory`() = runTest {
        val dataStore = EncryptionModule.provideKeysetDataStore(context)

        dataStore.edit { it[stringPreferencesKey("level")] = "DEVICE" }

        val file = File(context.noBackupFilesDir, EncryptionModule.KEYSET_FILE)
        assertThat(file).exists()
        assertThat(file.canonicalPath).startsWith(context.noBackupFilesDir.canonicalPath)
    }

    @Test
    fun `corrupted keyset file reads as empty instead of failing forever`() = runTest {
        val file = File(context.noBackupFilesDir, EncryptionModule.KEYSET_FILE)
        file.parentFile!!.mkdirs()
        file.writeBytes(byteArrayOf(0x13, 0x37, 0x00, 0x7F, 0x42))

        val dataStore = EncryptionModule.provideKeysetDataStore(context)

        assertThat(dataStore.data.first().asMap()).isEmpty()
    }
}
