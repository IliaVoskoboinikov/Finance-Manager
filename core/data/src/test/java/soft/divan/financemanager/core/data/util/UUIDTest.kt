package soft.divan.financemanager.core.data.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.UUID

class UUIDTest {

    @Test
    fun `generateUUID returns parseable uuid`() {
        val generated = generateUUID()

        assertThat(UUID.fromString(generated).toString()).isEqualTo(generated)
    }

    @Test
    fun `generateUUID returns unique values`() {
        val generated = List(100) { generateUUID() }

        assertThat(generated.toSet()).hasSize(generated.size)
    }
}
