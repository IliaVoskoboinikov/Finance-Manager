package soft.divan.financemanager.uikit.components

import androidx.compose.material.icons.Icons
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import soft.divan.financemanager.uikit.icons.AddRound

@Composable
fun FloatingButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    FloatingActionButton(
        onClick = { onClick() },
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        elevation = FloatingActionButtonDefaults.elevation(0.dp)
    ) {
        Icon(Icons.Filled.AddRound, contentDescription = "Add")
    }
}
