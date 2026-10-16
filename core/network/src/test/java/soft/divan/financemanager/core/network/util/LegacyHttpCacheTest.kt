package soft.divan.financemanager.core.network.util

import android.content.Context
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class LegacyHttpCacheTest {

    @get:Rule
    val folder = TemporaryFolder()

    private val context by lazy { mockk<Context> { every { cacheDir } returns folder.root } }

    @Test
    fun `old response cache is removed with its contents`() {
        val cache = File(folder.root, "http_cache").apply { mkdirs() }
        File(cache, "journal").writeText("libcore.io.DiskLruCache")
        File(cache, "0a1b.1").writeText("""{"balance":"1000.00"}""")
        val unrelated = File(folder.root, "images").apply { mkdirs() }

        LegacyHttpCache(context).delete()

        assertThat(cache).doesNotExist()
        assertThat(unrelated).exists()
    }

    @Test
    fun `missing cache is not an error`() {
        LegacyHttpCache(context).delete()

        assertThat(File(folder.root, "http_cache")).doesNotExist()
    }
}
