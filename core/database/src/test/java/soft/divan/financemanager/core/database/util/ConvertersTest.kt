package soft.divan.financemanager.core.database.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.database.model.SyncStatus

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `every SyncStatus survives a round trip`() {
        SyncStatus.entries.forEach { status ->
            val restored = converters.toSyncStatus(converters.fromSyncStatus(status))
            assertThat(restored).isEqualTo(status)
        }
    }

    @Test
    fun `fromSyncStatus writes the enum name`() {
        assertThat(converters.fromSyncStatus(SyncStatus.PENDING_CREATE))
            .isEqualTo("PENDING_CREATE")
    }

    @Test
    fun `unknown value falls back to SYNCED instead of throwing`() {
        assertThat(converters.toSyncStatus("SOMETHING_UNEXPECTED"))
            .isEqualTo(SyncStatus.SYNCED)
    }

    @Test
    fun `empty value falls back to SYNCED`() {
        assertThat(converters.toSyncStatus("")).isEqualTo(SyncStatus.SYNCED)
    }
}
