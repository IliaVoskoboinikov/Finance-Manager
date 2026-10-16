package soft.divan.financemanager.core.database.holder

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class DatabaseFilesTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val files = DatabaseFiles(context)

    @Test
    fun `nothing on a fresh install`() {
        assertThat(files.exists()).isFalse()
        assertThat(files.isPlaintext()).isFalse()
        assertThat(files.file.name).isEqualTo(DatabaseFiles.NAME)
    }

    @Test
    fun `unencrypted sqlite file is recognised by its header`() {
        files.file.parentFile!!.mkdirs()
        SQLiteDatabase.openOrCreateDatabase(files.file, null).use {
            it.execSQL("CREATE TABLE t (id INTEGER)")
        }

        assertThat(files.exists()).isTrue()
        assertThat(files.isPlaintext()).isTrue()
    }

    @Test
    fun `encrypted-looking file is not plaintext`() {
        writeDatabaseFile(ByteArray(4096) { (it * 31).toByte() })

        assertThat(files.isPlaintext()).isFalse()
    }

    @Test
    fun `file shorter than the header is not plaintext`() {
        writeDatabaseFile("SQLite".toByteArray())

        assertThat(files.isPlaintext()).isFalse()
    }

    @Test
    fun `unreadable file is not plaintext`() {
        // Каталог вместо файла: exists() == true, но прочитать заголовок нельзя
        files.file.mkdirs()

        assertThat(files.isPlaintext()).isFalse()
        files.file.delete()
    }

    @Test
    fun `delete removes the database together with its journals`() {
        writeDatabaseFile(ByteArray(16))
        val wal = File(files.file.path + "-wal").apply { writeBytes(ByteArray(8)) }
        val shm = File(files.file.path + "-shm").apply { writeBytes(ByteArray(8)) }

        files.delete()

        assertThat(files.exists()).isFalse()
        assertThat(wal).doesNotExist()
        assertThat(shm).doesNotExist()
    }

    private fun writeDatabaseFile(bytes: ByteArray) {
        files.file.parentFile!!.mkdirs()
        files.file.writeBytes(bytes)
    }
}
