package soft.divan.financemanager.core.data.mapper

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.data.dto.CategoryDto
import soft.divan.financemanager.core.database.entity.CategoryEntity
import java.time.Instant

class CategoryDataMapperTest {

    private val createdAt = "2024-01-01T00:00:00Z"
    private val updatedAt = "2024-02-01T12:30:00Z"

    @Test
    fun `CategoryDto toEntity maps all fields`() {
        val dto = CategoryDto(
            id = "1",
            createdAt = createdAt,
            updatedAt = updatedAt,
            name = "Food",
            emoji = "🍔",
            isIncome = false
        )

        val entity = dto.toEntity()

        assertThat(entity.id).isEqualTo("1")
        assertThat(entity.createdAt).isEqualTo(createdAt)
        assertThat(entity.updatedAt).isEqualTo(updatedAt)
        assertThat(entity.name).isEqualTo("Food")
        assertThat(entity.emoji).isEqualTo("🍔")
        assertThat(entity.isIncome).isFalse()
    }

    @Test
    fun `CategoryEntity toDomain maps all fields and parses dates`() {
        val entity = CategoryEntity(
            id = "2",
            createdAt = createdAt,
            updatedAt = updatedAt,
            name = "Salary",
            emoji = "💰",
            isIncome = true
        )

        val domain = entity.toDomain()

        assertThat(domain.id).isEqualTo("2")
        assertThat(domain.createdAt).isEqualTo(Instant.parse(createdAt))
        assertThat(domain.updatedAt).isEqualTo(Instant.parse(updatedAt))
        assertThat(domain.name).isEqualTo("Salary")
        assertThat(domain.emoji).isEqualTo("💰")
        assertThat(domain.isIncome).isTrue()
    }

    @Test
    fun `CategoryEntity toDomain normalizes offset timestamps to instants`() {
        val entity = CategoryEntity(
            id = "3",
            createdAt = "2024-01-01T12:00:00+03:00",
            updatedAt = "2024-01-01T12:00:00+03:00",
            name = "Gifts",
            emoji = "🎁",
            isIncome = false
        )

        val domain = entity.toDomain()

        assertThat(domain.createdAt).isEqualTo(Instant.parse("2024-01-01T09:00:00Z"))
    }
}
