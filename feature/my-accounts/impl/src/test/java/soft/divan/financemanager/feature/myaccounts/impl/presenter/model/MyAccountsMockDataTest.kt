package soft.divan.financemanager.feature.myaccounts.impl.presenter.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MyAccountsMockDataTest {

    @Test
    fun `mock accounts have unique ids and filled fields`() {
        assertThat(mockAccountsUi).isNotEmpty()
        assertThat(mockAccountsUi.map { it.id }).doesNotHaveDuplicates()
        mockAccountsUi.forEach { account ->
            assertThat(account.name).isNotEmpty()
            assertThat(account.balance).isNotEmpty()
        }
    }

    @Test
    fun `preview mock data covers all ui states`() {
        assertThat(mockMyAccountsUiStateLoading).isEqualTo(MyAccountsUiState.Loading)
        assertThat(mockMyAccountsUiStateSuccess.accounts).isEqualTo(mockAccountsUi)
        assertThat(mockMyAccountsUiStateError).isInstanceOf(MyAccountsUiState.Error::class.java)
    }
}
