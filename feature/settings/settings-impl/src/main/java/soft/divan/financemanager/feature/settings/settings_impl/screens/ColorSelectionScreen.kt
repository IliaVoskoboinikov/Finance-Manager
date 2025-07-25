package soft.divan.financemanager.feature.settings.settings_impl.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import soft.divan.financemanager.feature.settings.settings_impl.viewModel.CollorSelectionViewModel
import soft.divan.financemanager.uikit.components.TopBar
import soft.divan.financemanager.uikit.model.TopBarModel
import soft.divan.financemanager.uikit.theme.AccentColor

@Composable
fun ColorSelectionScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: CollorSelectionViewModel = hiltViewModel()
) {
    val selectedColor by viewModel.accentColor.collectAsState()
    val colorOptions = AccentColor.entries


    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {


            TopBar(
                topBar = TopBarModel(title = soft.divan.financemanager.string.R.string.settings),
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(colorOptions) { color ->
                    val isSelected = color == selectedColor
                    val colorPreview = getColorForAccent(color)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .clickable {
                                viewModel.setAccentColor(color)

                            },
                        colors = CardDefaults.cardColors(
                            containerColor = colorPreview,
                            contentColor = contentColorFor(colorPreview)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = color.name.lowercase().replaceFirstChar { it.titlecase() },
                                style = MaterialTheme.typography.bodyLarge
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = contentColorFor(colorPreview)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun getColorForAccent(accentColor: AccentColor): Color {
    return when (accentColor) {
        AccentColor.MINT -> Color(0xFF00E5A0)
        AccentColor.PURPLE -> Color(0xFF9C27B0)
        AccentColor.ORANGE -> Color(0xFFFF9800)
        AccentColor.BLUE -> Color(0xFF2196F3)
        AccentColor.PINK -> Color(0xFFE91E63)
    }
}
