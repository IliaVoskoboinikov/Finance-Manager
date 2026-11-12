package soft.divan.financemanager.core.loggingerror

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class RedactedThrowableTest {

    @Test
    fun `text is dropped from every link of the cause chain`() {
        val root = NumberFormatException("Number has unsupported scale: 1234.56")
        val top = IllegalStateException("Failed parsing '1234.56' as BigDecimal", root)

        val redacted = top.redacted()

        assertThat(chainOf(redacted).map { it.message }).containsExactly(
            IllegalStateException::class.java.name,
            NumberFormatException::class.java.name
        )
        assertThat(chainOf(redacted).joinToString { it.toString() }).doesNotContain("1234.56")
    }

    @Test
    fun `stack of every link is kept`() {
        val root = IllegalArgumentException("root")
        val top = RuntimeException("top", root)

        val redacted = top.redacted()

        assertThat(redacted.stackTrace).isEqualTo(top.stackTrace)
        assertThat(redacted.cause?.stackTrace).isEqualTo(root.stackTrace)
    }

    @Test
    fun `exception without text keeps its type`() {
        val redacted = IllegalStateException().redacted()

        assertThat(redacted.message).isEqualTo(IllegalStateException::class.java.name)
        assertThat(redacted.cause).isNull()
    }

    @Test
    fun `cyclic cause chain terminates`() {
        val first = RuntimeException("first")
        val second = RuntimeException("second", first)
        first.initCause(second)

        val redacted = first.redacted()

        // Повторное звено обрывает цепочку: first -> second, без возврата к first
        assertThat(chainOf(redacted)).hasSize(2)
    }

    @Test
    fun `very long cause chain is cut`() {
        val deep = (1..100).fold(RuntimeException("0")) { cause, i -> RuntimeException("$i", cause) }

        assertThat(chainOf(deep.redacted())).hasSize(MAX_CAUSE_DEPTH)
    }

    @Test
    fun `suppressed exceptions are not carried over`() {
        val top = IllegalStateException("top").apply { addSuppressed(RuntimeException("1234.56")) }

        assertThat(top.redacted().suppressed).isEmpty()
    }

    private fun chainOf(throwable: Throwable): List<Throwable> =
        generateSequence(throwable) { it.cause }.toList()
}
