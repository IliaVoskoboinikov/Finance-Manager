package soft.divan.financemanager.core.data.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

class ApiDateMapperTest {

    @Test
    fun `toApiDate formats instant as ISO local date in given zone`() {
        val instant = Instant.parse("2024-03-07T12:00:00Z")

        assertThat(ApiDateMapper.toApiDate(instant, ZoneOffset.UTC)).isEqualTo("2024-03-07")
    }

    @Test
    fun `toApiDate shifts date forward across midnight for positive offset`() {
        val instant = Instant.parse("2024-03-07T23:30:00Z")

        assertThat(ApiDateMapper.toApiDate(instant, ZoneId.of("+03:00"))).isEqualTo("2024-03-08")
    }

    @Test
    fun `toApiDate shifts date backward across midnight for negative offset`() {
        val instant = Instant.parse("2024-03-07T00:30:00Z")

        assertThat(ApiDateMapper.toApiDate(instant, ZoneId.of("-03:00"))).isEqualTo("2024-03-06")
    }

    @Test
    fun `toApiDate defaults to system zone`() {
        val instant = Instant.parse("2024-03-07T12:00:00Z")

        val expected = ApiDateMapper.toApiDate(instant, ZoneId.systemDefault())

        assertThat(ApiDateMapper.toApiDate(instant)).isEqualTo(expected)
    }
}
