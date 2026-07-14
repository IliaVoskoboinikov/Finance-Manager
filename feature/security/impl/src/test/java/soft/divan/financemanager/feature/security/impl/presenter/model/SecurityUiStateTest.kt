package soft.divan.financemanager.feature.security.impl.presenter.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class SecurityUiStateTest {

    @Test
    fun `success carries pin flag`() {
        assertThat(SecurityUiState.Success(hasPin = true).hasPin).isTrue()
        assertThat(SecurityUiState.Success().hasPin).isFalse()
    }

    @Test
    fun `error carries message resource`() {
        assertThat(SecurityUiState.Error(messageRes = 7).messageRes).isEqualTo(7)
    }

    @Test
    fun `loading is a singleton`() {
        assertThat(SecurityUiState.Loading).isSameAs(SecurityUiState.Loading)
    }
}
