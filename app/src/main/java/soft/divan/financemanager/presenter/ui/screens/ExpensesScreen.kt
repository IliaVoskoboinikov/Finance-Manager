package soft.divan.financemanager.presenter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import soft.divan.financemanager.R
import soft.divan.financemanager.presenter.ui.theme.FinanceManagerTheme
import soft.divan.financemanager.presenter.uiKit.FMDriver
import soft.divan.financemanager.presenter.uiKit.FloatingButton
import soft.divan.financemanager.presenter.uiKit.ListItem
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import soft.divan.financemanager.presenter.uiKit.ContentTextListItem
import soft.divan.financemanager.presenter.uiKit.SubContentTextListItem
import soft.divan.financemanager.presenter.ui.icons.Arrow

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun InfoScreenPreview() {
    FinanceManagerTheme {
        ExpensesScreen(navController = rememberNavController())
    }
}

@Composable
fun ExpensesScreen(modifier: Modifier = Modifier, navController: NavController) {

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingButton(onClick = {})
        }
    ) { innerPadding ->

        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(items) { item ->
                RenderExpensesListItem(item)
                FMDriver()
            }
        }
    }
}

private val items = listOf(
    ExpensesListItemModel.All(
        content = R.string.all,
        trail = "436 558 ₽"
    ),
    ExpensesListItemModel.Item(
        emoji = "\uD83C\uDFE1",
        content = "Аренда квартиры",
        prise = "100 000 ₽",
        onClick = {}
    ),
    ExpensesListItemModel.Item(
        emoji = "\uD83D\uDC57",
        content = "Одежда",
        prise = "100 000 ₽",
        onClick = {}
    ),
    ExpensesListItemModel.ItemSubContent(
        emoji = "\uD83D\uDC36",
        content = "На собачку",
        subContent = "Джек",
        prise = "100 000 ₽",
        onClick = {}
    ),
    ExpensesListItemModel.ItemSubContent(
        emoji = "\uD83D\uDC36",
        content = "На собачку",
        subContent = "Энни",
        prise = "100 000 ₽",
        onClick = {}
    ),
    ExpensesListItemModel.Item(
        emoji = "pk",
        content = "Ремонт квартиры",
        prise = "100 000 ₽",
        onClick = {}
    ),
    ExpensesListItemModel.Item(
        emoji = "\uD83C\uDF6D",
        content = "Продукты",
        prise = "100 000 ₽",
        onClick = {}
    ),
    ExpensesListItemModel.Item(
        emoji = "\uD83C\uDFCB\uFE0F",
        content = "Спортзал",
        prise = "100 000 ₽",
        onClick = {}
    ),
    ExpensesListItemModel.Item(
        emoji = "\uD83D\uDC8A",
        content = "Медицина",
        prise = "100 000 ₽",
        onClick = {}
    )


)

sealed class ExpensesListItemModel {
    data class All(
        val content: Int,
        val trail: String
    ) : ExpensesListItemModel()

    data class Item(
        val emoji: String,
        val content: String,
        val prise: String,
        val onClick: () -> Unit
    ) : ExpensesListItemModel()

    data class ItemSubContent(
        val emoji: String,
        val content: String,
        val subContent: String,
        val prise: String,
        val onClick: () -> Unit
    ) : ExpensesListItemModel()
}


@Composable
fun RenderExpensesListItem(model: ExpensesListItemModel) {
    when (model) {
        is ExpensesListItemModel.All -> {

            ListItem(
                modifier = Modifier.height(70.dp),
                content = { ContentTextListItem(stringResource(model.content)) },
                trail = { ContentTextListItem(model.trail) },
                containerColor = colorScheme.secondaryContainer
            )
        }

        is ExpensesListItemModel.Item -> {
            ListItem(
                modifier = Modifier.height(70.dp),
                lead = {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    )
                    {
                        Text(text = model.emoji, textAlign = TextAlign.Center)

                    }

                },
                content = { ContentTextListItem(model.content) },
                trail = {

                    ContentTextListItem(model.prise)
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Filled.Arrow,
                        contentDescription = "arrow",
                        tint = colorScheme.onSurfaceVariant

                    )
                },

                )
        }

        is ExpensesListItemModel.ItemSubContent -> {
            ListItem(
                modifier = Modifier.height(70.dp),
                lead = {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    )
                    {
                        Text(text = model.emoji, textAlign = TextAlign.Center)

                    }

                },
                content = {
                    Column() {
                        ContentTextListItem(model.content)
                        SubContentTextListItem(model.subContent)
                    }
                },
                trail = {

                    ContentTextListItem(model.prise)
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Filled.Arrow,
                        contentDescription = "arrow",
                        tint = colorScheme.onSurfaceVariant

                    )
                },

                )
        }
    }
}