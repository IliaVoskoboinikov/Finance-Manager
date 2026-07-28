package soft.divan.financemanager.presenter.navigation

import androidx.compose.material.icons.Icons
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import soft.divan.financemanager.R
import soft.divan.financemanager.core.featureapi.FeatureApi
import soft.divan.financemanager.feature.transactionstoday.api.TransactionsTodayFeatureApi
import soft.divan.financemanager.uikit.icons.Calculator
import soft.divan.financemanager.uikit.icons.Chart90
import soft.divan.financemanager.uikit.icons.Downtrend
import soft.divan.financemanager.uikit.icons.Settings
import soft.divan.financemanager.uikit.icons.Uptrend

class ScreenBottomTest {

    private val transactionsToday = mockk<TransactionsTodayFeatureApi> {
        every { expenseRoute } returns "expenses"
        every { incomeRoute } returns "income"
    }
    private val myAccounts = mockk<FeatureApi> { every { route } returns "my_accounts" }
    private val category = mockk<FeatureApi> { every { route } returns "category" }
    private val settings = mockk<FeatureApi> { every { route } returns "settings" }

    private val items = ScreenBottom.items(transactionsToday, myAccounts, category, settings)

    @Test
    fun `items build five bottom destinations in fixed order`() {
        assertThat(items.map { it.route })
            .containsExactly("expenses", "income", "my_accounts", "category", "settings")
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
