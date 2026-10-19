package soft.divan.financemanager.presenter.navigation

import androidx.compose.runtime.mutableIntStateOf
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

@Serializable
private data object ExpensesTab : NavKey

@Serializable
private data object AccountsTab : NavKey

@Serializable
private data object SettingsTab : NavKey

@Serializable
private data class DetailKey(val id: String) : NavKey

class TopLevelBackStackTest {

    private val tabKeys = listOf<NavKey>(ExpensesTab, AccountsTab, SettingsTab)

    private fun backStack(): TopLevelBackStack = TopLevelBackStack(
        tabKeys = tabKeys,
        tabStacks = tabKeys.map { NavBackStack(it) },
        currentTabIndex = mutableIntStateOf(0)
    )

    @Test
    fun `starts on the first tab`() {
        val backStack = backStack()

        assertThat(backStack.currentTabKey).isEqualTo(ExpensesTab)
        assertThat(backStack.displayStack).containsExactly(ExpensesTab)
    }

    @Test
    fun `goTo pushes screen onto the current tab stack`() {
        val backStack = backStack()

        backStack.goTo(DetailKey("1"))

        assertThat(backStack.displayStack).containsExactly(ExpensesTab, DetailKey("1"))
    }

    @Test
    fun `goTo ignores the key that is already on top`() {
        val backStack = backStack()

        backStack.goTo(DetailKey("1"))
        backStack.goTo(DetailKey("1"))

        assertThat(backStack.displayStack).containsExactly(ExpensesTab, DetailKey("1"))
    }

    @Test
    fun `goTo returns to a key already in the stack instead of pushing a duplicate`() {
        val backStack = backStack()

        backStack.goTo(DetailKey("1"))
        backStack.goTo(DetailKey("2"))
        backStack.goTo(DetailKey("1"))

        assertThat(backStack.displayStack).containsExactly(ExpensesTab, DetailKey("1"))
    }

    @Test
    fun `goTo to the tab root returns to the root`() {
        val backStack = backStack()
        backStack.goTo(DetailKey("1"))

        backStack.goTo(ExpensesTab)

        assertThat(backStack.displayStack).containsExactly(ExpensesTab)
    }

    @Test
    fun `back from the top screen pops it`() {
        val backStack = backStack()
        backStack.goTo(DetailKey("1"))

        backStack.back(from = DetailKey("1"))

        assertThat(backStack.displayStack).containsExactly(ExpensesTab)
    }

    @Test
    fun `repeated back from an already closed screen keeps the screen below`() {
        val backStack = backStack()
        backStack.goTo(DetailKey("list"))
        backStack.goTo(DetailKey("form"))

        backStack.back(from = DetailKey("form"))
        backStack.back(from = DetailKey("form"))

        assertThat(backStack.displayStack).containsExactly(ExpensesTab, DetailKey("list"))
    }

    @Test
    fun `back from another tab root returns to the start tab when it is on top`() {
        val backStack = backStack()
        backStack.switchTab(SettingsTab)

        backStack.back(from = SettingsTab)

        assertThat(backStack.currentTabKey).isEqualTo(ExpensesTab)
    }

    @Test
    fun `back pops the last screen of the current tab`() {
        val backStack = backStack()
        backStack.goTo(DetailKey("1"))

        backStack.back()

        assertThat(backStack.displayStack).containsExactly(ExpensesTab)
    }

    @Test
    fun `start tab root keeps display stack under another tab`() {
        val backStack = backStack()

        backStack.switchTab(SettingsTab)

        assertThat(backStack.displayStack).containsExactly(ExpensesTab, SettingsTab)
    }

    @Test
    fun `back from another tab root returns to the start tab`() {
        val backStack = backStack()
        backStack.switchTab(SettingsTab)

        backStack.back()

        assertThat(backStack.currentTabKey).isEqualTo(ExpensesTab)
        assertThat(backStack.displayStack).containsExactly(ExpensesTab)
    }

    @Test
    fun `back on the start tab root does nothing so the system closes the app`() {
        val backStack = backStack()

        backStack.back()

        assertThat(backStack.displayStack).containsExactly(ExpensesTab)
    }

    @Test
    fun `each tab keeps its own stack when switching`() {
        val backStack = backStack()
        backStack.goTo(DetailKey("expenses"))

        backStack.switchTab(AccountsTab)
        backStack.goTo(DetailKey("accounts"))
        backStack.switchTab(ExpensesTab)

        assertThat(backStack.displayStack)
            .containsExactly(ExpensesTab, DetailKey("expenses"))

        backStack.switchTab(AccountsTab)

        assertThat(backStack.displayStack)
            .containsExactly(ExpensesTab, AccountsTab, DetailKey("accounts"))
    }

    @Test
    fun `switching to an unknown tab fails`() {
        val backStack = backStack()

        assertThatThrownBy { backStack.switchTab(DetailKey("nope")) }
            .isInstanceOf(IllegalArgumentException::class.java)
    }
}
