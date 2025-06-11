package soft.divan.financemanager.presenter.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import soft.divan.financemanager.R
import soft.divan.financemanager.presenter.uiKit.ContentTextListItem
import soft.divan.financemanager.presenter.uiKit.FMDriver
import soft.divan.financemanager.presenter.uiKit.FloatingButton
import soft.divan.financemanager.presenter.uiKit.ListItem
import soft.divan.financemanager.presenter.ui.icons.Arrow
import soft.divan.financemanager.presenter.ui.icons.Diagram
import soft.divan.financemanager.presenter.ui.theme.FinanceManagerTheme


@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun AccountScreenPreview() {
    FinanceManagerTheme {
        AccountScreen(navController = rememberNavController())
    }
}

@Composable
fun AccountScreen(modifier: Modifier = Modifier, navController: NavController) {
    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingButton(onClick = {})
        }
    ) { innerPadding ->

        Column(modifier = Modifier.padding(innerPadding)) {
            LazyColumn {
                items(items) { item ->
                    RenderAccountListItem(item)
                    FMDriver()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Icon(
                imageVector = Icons.Filled.Diagram,
                contentDescription = "Diagram",
                modifier = Modifier.fillMaxWidth(),
                tint = Color.Unspecified
            )


        }
    }
}

private val items = listOf(
    AccountListItemModel.Balance(
        emoji = "\uD83D\uDCB0",
        content = R.string.all,
        trail = "-670 000 ₽",
        onClick = {}
    ),
    AccountListItemModel.Currency(
        content = "Валюта",
        prise = "₽",
        onClick = {}
    ),


    )

sealed class AccountListItemModel {
    data class Balance(
        val emoji: String,
        val content: Int,
        val trail: String,
        val onClick: () -> Unit
    ) : AccountListItemModel()

    data class Currency(
        val content: String,
        val prise: String,
        val onClick: () -> Unit
    ) : AccountListItemModel()

}


@Composable
fun RenderAccountListItem(model: AccountListItemModel) {
    when (model) {
        is AccountListItemModel.Balance -> {
            ListItem(
                modifier = Modifier.height(56.dp),
                lead = {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    )
                    {
                        Text(text = model.emoji, textAlign = TextAlign.Center)

                    }
                },
                content = { ContentTextListItem(stringResource(model.content)) },
                trail = {
                    ContentTextListItem(model.trail)
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Filled.Arrow,
                        contentDescription = "arrow",
                        tint = colorScheme.onSurfaceVariant

                    )
                },
                containerColor = colorScheme.secondaryContainer
            )
        }

        is AccountListItemModel.Currency -> {
            ListItem(
                modifier = Modifier.height(56.dp),
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
                containerColor = colorScheme.secondaryContainer
            )
        }
    }
}