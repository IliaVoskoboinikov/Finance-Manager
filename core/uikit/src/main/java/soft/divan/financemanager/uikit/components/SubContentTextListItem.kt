package soft.divan.financemanager.uikit.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

@Composable
fun SubContentTextListItem(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(
            fontWeight = FontWeight.W400,
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
            letterSpacing = MaterialTheme.typography.bodyMedium.letterSpacing,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = modifier
    )
}
// Revue me>>
