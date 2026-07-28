package soft.divan.financemanager.lint

import com.android.tools.lint.client.api.LintClient
import com.android.tools.lint.detector.api.CURRENT_API
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class FmIssueRegistryTest {

    private val registry = FmIssueRegistry()

    private companion object {
        init {
            LintClient.clientName = LintClient.CLIENT_UNIT_TESTS
        }
    }

    @Test
    fun `registry exposes AvoidDate issue`() {
        assertEquals(listOf(AvoidDateDetector.ISSUE), registry.issues)
    }

    @Test
    fun `registry targets current lint api`() {
        assertEquals(CURRENT_API, registry.api)
    }

    @Test
    fun `registry supports lint api back to 8`() {
        assertEquals(8, registry.minApi)
    }

    @Test
    fun `registry declares vendor metadata`() {
        assertEquals("soft-divan", registry.vendor.vendorName)
        assertFalse(registry.vendor.feedbackUrl.isNullOrBlank())
        assertFalse(registry.vendor.contact.isNullOrBlank())
    }
}
