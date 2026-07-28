package soft.divan.financemanager.core.domain.utli

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.core.domain.model.TransactionType

class EnumMapperTest {

    @Test
    fun `toEnumOrNull returns constant for exact name`() {
        assertThat("INCOME".toEnumOrNull<TransactionType>()).isEqualTo(TransactionType.INCOME)
    }

    @Test
    fun `toEnumOrNull ignores case`() {
        assertThat("income".toEnumOrNull<TransactionType>()).isEqualTo(TransactionType.INCOME)
        assertThat("ExPeNsE".toEnumOrNull<TransactionType>()).isEqualTo(TransactionType.EXPENSE)
    }

    @Test
    fun `toEnumOrNull resolves every constant of the enum`() {
        TransactionType.entries.forEach { type ->
            assertThat(type.name.lowercase().toEnumOrNull<TransactionType>()).isEqualTo(type)
        }
    }

    @Test
    fun `toEnumOrNull returns null for unknown name`() {
        assertThat("UNKNOWN_TYPE".toEnumOrNull<TransactionType>()).isNull()
    }

    @Test
    fun `toEnumOrNull returns null for empty string`() {
        assertThat("".toEnumOrNull<TransactionType>()).isNull()
    }

    @Test
    fun `toEnumOrNull does not match partial names`() {
        assertThat("INCOM".toEnumOrNull<TransactionType>()).isNull()
        assertThat("INCOMES".toEnumOrNull<TransactionType>()).isNull()
    }
}
