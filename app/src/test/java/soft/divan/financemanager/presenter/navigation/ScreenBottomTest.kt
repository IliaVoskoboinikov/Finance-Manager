package soft.divan.financemanager.presenter.navigation

import androidx.compose.material.icons.Icons
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.R
import soft.divan.financemanager.feature.category.api.CategoryKey
import soft.divan.financemanager.feature.myaccounts.impl.MyAccountsKey
import soft.divan.financemanager.feature.settings.api.SettingsKey
import soft.divan.financemanager.feature.transactionstoday.api.TransactionsTodayKey
import soft.divan.financemanager.uikit.icons.Calculator
import soft.divan.financemanager.uikit.icons.Chart90
import soft.divan.financemanager.uikit.icons.Downtrend
import soft.divan.financemanager.uikit.icons.Settings
import soft.divan.financemanager.uikit.icons.Uptrend

class ScreenBottomTest {

    private val items = ScreenBottom.items()

    @Test
    fun `items build five bottom destinations in fixed order`() {
        assertThat(items.map { it.key }).containsExactly(
            TransactionsTodayKey(isIncome = false),
            TransactionsTodayKey(isIncome = true),
            MyAccountsKey,
            CategoryKey,
            SettingsKey
        )
    }

    @Test
    fun `expenses and income are different tabs of the same screen`() {
        val expenses = items[0].key as TransactionsTodayKey
        val income = items[1].key as TransactionsTodayKey

        assertThat(expenses.isIncome).isFalse()
        assertThat(income.isIncome).isTrue()
        assertThat(expenses).isNotEqualTo(income)
    }

    @Test
    fun `items use feature titles`() {
        assertThat(items.map { it.title }).containsExactly(
            R.string.expenses,
            R.string.income,
            R.string.account,
            R.string.category,
            R.string.settings
        )
    }

    @Test
    fun `items use distinct icons`() {
        assertThat(items.map { it.icon }).containsExactly(
            Icons.Filled.Downtrend,
            Icons.Filled.Uptrend,
            Icons.Filled.Calculator,
            Icons.Filled.Chart90,
            Icons.Filled.Settings
        )
    }
}
