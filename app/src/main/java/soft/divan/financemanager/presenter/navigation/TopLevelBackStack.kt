package soft.divan.financemanager.presenter.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import soft.divan.financemanager.core.featureapi.Navigator

/**
 * Back stack нижней навигации: у каждой вкладки свой независимый стек экранов.
 *
 * Navigation 3 не предлагает готового многостекового хоста — стеки держит хост, а
 * `NavDisplay` показывает тот список, который ему дали. Здесь это [displayStack]:
 * экраны выбранной вкладки, под которыми лежит корень стартовой вкладки. За счёт этого
 * системный «назад» с корня любой вкладки возвращает на стартовую — так же, как это делал
 * `popUpTo(startDestination) { saveState = true }` на Navigation 2.
 *
 * Стеки создаются через `rememberNavBackStack`, поэтому переживают смерть процесса.
 */
@Stable
class TopLevelBackStack(
    private val tabKeys: List<NavKey>,
    private val tabStacks: List<NavBackStack<NavKey>>,
    private val currentTabIndex: MutableIntState
) : Navigator {

    init {
        require(tabKeys.isNotEmpty()) { "Нижняя навигация должна содержать хотя бы одну вкладку" }
        require(tabKeys.size == tabStacks.size) { "У каждой вкладки должен быть свой стек" }
    }

    /** Ключ выбранной вкладки. */
    val currentTabKey: NavKey
        get() = tabKeys[currentTabIndex.intValue]

    /** Список экранов для `NavDisplay`. */
    val displayStack: List<NavKey>
        get() = if (currentTabIndex.intValue == START_TAB_INDEX) {
            currentStack.toList()
        } else {
            listOf(tabKeys[START_TAB_INDEX]) + currentStack
        }

    private val currentStack: NavBackStack<NavKey>
        get() = tabStacks[currentTabIndex.intValue]

    /** Переключает вкладку, сохраняя стек предыдущей. */
    fun switchTab(tabKey: NavKey) {
        val index = tabKeys.indexOf(tabKey)
        require(index >= 0) { "Вкладка $tabKey отсутствует в нижней навигации" }
        currentTabIndex.intValue = index
    }

    override fun goTo(key: NavKey) {
        currentStack.add(key)
    }

    override fun back() {
        when {
            currentStack.size > 1 -> currentStack.removeAt(currentStack.lastIndex)

            currentTabIndex.intValue != START_TAB_INDEX ->
                currentTabIndex.intValue = START_TAB_INDEX
        }
    }

    private companion object {
        const val START_TAB_INDEX = 0
    }
}

/**
 * Создаёт [TopLevelBackStack] для [tabs]: по сохраняемому стеку на вкладку плюс
 * сохраняемый индекс выбранной вкладки.
 */
@Composable
fun rememberTopLevelBackStack(tabs: List<ScreenBottom>): TopLevelBackStack {
    val tabKeys = tabs.map { it.key }
    val tabStacks = tabKeys.map { tabKey -> key(tabKey) { rememberNavBackStack(tabKey) } }
    val currentTabIndex = rememberSaveable { mutableIntStateOf(0) }

    return remember {
        TopLevelBackStack(
            tabKeys = tabKeys,
            tabStacks = tabStacks,
            currentTabIndex = currentTabIndex
        )
    }
}
