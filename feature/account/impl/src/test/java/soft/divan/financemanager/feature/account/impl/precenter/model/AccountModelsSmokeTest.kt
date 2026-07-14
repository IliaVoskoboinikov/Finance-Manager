package soft.divan.financemanager.feature.account.impl.precenter.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class AccountModelsSmokeTest {

    @Test
    fun `actions invoke provided handlers`() {
        var calls = 0
        val actions = AccountActions(
            onNavigateBack = { calls++ },
            onUpdateName = { calls++ },
            onUpdateBalance = { calls++ },
            onUpdateCurrency = { calls++ },
            onSave = { calls++ },
            onDelete = { calls++ }
        )

        actions.onNavigateBack()
        actions.onUpdateName("n")
        actions.onUpdateBalance("1")
        actions.onUpdateCurrency("rub")
        actions.onSave()
        actions.onDelete()

        assertThat(calls).isEqualTo(6)
    }

    @Test
    fun `preview mock data covers all ui states`() {
        assertThat(mockAccountUiStateLoading).isEqualTo(AccountUiState.Loading)
        assertThat(mockAccountUiStateSuccess.account.name).isNotEmpty()
        assertThat(mockAccountUiStateSuccess.mode).isEqualTo(AccountMode.Create)
        assertThat(mockAccountUiStateError).isInstanceOf(AccountUiState.Error::class.java)
    }
}
