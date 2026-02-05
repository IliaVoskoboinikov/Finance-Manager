package soft.divan.financemanager.uikit.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import soft.divan.financemanager.uikit.icons.Arrow
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun ListItemPreview() {
    FinanceManagerTheme {
        ListItem(
            content = { Text("Дата") },
            trail = {
                Text("25.02.2025")
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Filled.Arrow,
                    contentDescription = ""
                )
            }

        )
    }
}

@Composable
fun ListItem(
    modifier: Modifier = Modifier,
    lead: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
    trail: (@Composable () -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentPadding: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
) {
    Surface(
        modifier = modifier,
        color = containerColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            lead?.let {
                it()
                Spacer(modifier = Modifier.width(16.dp))
            }

            Box(modifier = Modifier.weight(1f)) {
                content()
            }

            trail?.let {
                Spacer(modifier = Modifier.width(16.dp))
                it()
            }
        }
    }
}
