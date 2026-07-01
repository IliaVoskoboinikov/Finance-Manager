package soft.divan.financemanager.feature.security.impl.data.crypto

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PinHasherTest {

    @Test
    fun `hash produces salt and hash separated by a single colon`() {
        val parts = PinHasher.hash(PIN).split(":")

        assertThat(parts).hasSize(2)
        assertThat(parts[0]).isNotEmpty()
        assertThat(parts[1]).isNotEmpty()
    }

    @Test
    fun `verify returns true for the correct pin`() {
        val stored = PinHasher.hash(PIN)

        assertThat(PinHasher.verify(PIN, stored)).isTrue()
    }

    @Test
    fun `verify returns false for a wrong pin`() {
        val stored = PinHasher.hash(PIN)

        assertThat(PinHasher.verify(WRONG_PIN, stored)).isFalse()
    }

    @Test
    fun `verify distinguishes pins that differ by one digit`() {
        val stored = PinHasher.hash("1234")

        assertThat(PinHasher.verify("1235", stored)).isFalse()
    }

    @Test
    fun `hash uses a random salt so two hashes of the same pin differ`() {
        assertThat(PinHasher.hash(PIN)).isNotEqualTo(PinHasher.hash(PIN))
    }

    @Test
    fun `verify succeeds for the same pin hashed with different salts`() {
        val first = PinHasher.hash(PIN)
        val second = PinHasher.hash(PIN)

        assertThat(PinHasher.verify(PIN, first)).isTrue()
        assertThat(PinHasher.verify(PIN, second)).isTrue()
    }

    @Test
    fun `hash and verify round-trip for various pins`() {
        listOf("0000", "9999", "1234", "5678").forEach { pin ->
            assertThat(PinHasher.verify(pin, PinHasher.hash(pin))).isTrue()
        }
    }

    @Test
    fun `verify returns false when stored value has no separator`() {
        assertThat(PinHasher.verify(PIN, "not-a-valid-hash")).isFalse()
    }

    @Test
    fun `verify returns false for an empty stored value`() {
        assertThat(PinHasher.verify(PIN, "")).isFalse()
    }

    @Test
    fun `verify returns false when stored value has extra separators`() {
        val stored = PinHasher.hash(PIN)

        assertThat(PinHasher.verify(PIN, "$stored:extra")).isFalse()
    }

    @Test
    fun `verify returns false for invalid base64 in stored value`() {
        assertThat(PinHasher.verify(PIN, "@@@:@@@")).isFalse()
    }

    private companion object {
        const val PIN = "1234"
        const val WRONG_PIN = "0000"
    }
}
