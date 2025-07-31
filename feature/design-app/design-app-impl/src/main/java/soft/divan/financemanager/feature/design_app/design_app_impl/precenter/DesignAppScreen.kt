package soft.divan.financemanager.feature.design_app.design_app_impl.precenter

import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import soft.divan.financemanager.feature.design_app.design_app_impl.R
import soft.divan.financemanager.feature.design_app.design_app_impl.domain.model.ThemeMode
import soft.divan.financemanager.uikit.components.TopBar
import soft.divan.financemanager.uikit.model.TopBarModel
import soft.divan.financemanager.uikit.theme.AccentColor


@Composable
fun DesignAppScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: DesignAppViewModel = hiltViewModel()
) {


    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    val selectedColor by viewModel.accentColor.collectAsStateWithLifecycle()
    var showColorPicker by remember { mutableStateOf(false) }


    val colorOptions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AccentColor.entries
        } else {
            AccentColor.entries.filter { it != AccentColor.DYNAMIC }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {


            TopBar(topBar = TopBarModel(title = R.string.design))

            if (showColorPicker) {
                CustomColorPickerDialog(
                    initialColor = Color(0xFF6200EE),
                    onDismissRequest = { showColorPicker = false },
                    onColorSelected = { selectedColor ->
                        viewModel.setCustomColor(selectedColor)
                        showColorPicker = false
                    }
                )
            }


            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // --- Блок выбора темы ---
                item {
                    Text(
                        text = stringResource(R.string.choice_theme),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                item {
                    Spacer(Modifier.height(8.dp))
                }

                item {
                    ThemeSwitchItem(
                        title = stringResource(R.string.light),
                        selected = themeMode == ThemeMode.LIGHT,
                        onClick = { viewModel.onThemeSelected(ThemeMode.LIGHT) }
                    )
                }

                item {
                    ThemeSwitchItem(
                        title = stringResource(R.string.dark),
                        selected = themeMode == ThemeMode.DARK,
                        onClick = { viewModel.onThemeSelected(ThemeMode.DARK) }
                    )
                }

                item {
                    ThemeSwitchItem(
                        title = stringResource(R.string.system),
                        selected = themeMode == ThemeMode.SYSTEM,
                        onClick = { viewModel.onThemeSelected(ThemeMode.SYSTEM) }
                    )
                }

                // --- Блок цветовой палитры ---
                item {
                    Spacer(Modifier.height(24.dp))
                    Text(
                        text = stringResource(R.string.color_palette),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                }

                items(colorOptions) { color ->
                    val isSelected = color == selectedColor

                    if (color == AccentColor.CUSTOM) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clickable { showColorPicker = true }
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Red, Color.Yellow,
                                            Color.Green, Color.Cyan, Color.Blue, Color.Magenta
                                        )
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = color.name.lowercase().replaceFirstChar { it.titlecase() },
                                    style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    } else {
                        val colorPreview = getColorForAccent(color)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clickable { viewModel.setAccentColor(color) },
                            colors = CardDefaults.cardColors(
                                containerColor = colorPreview,
                                contentColor = contentColorFor(colorPreview)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            shape = RoundedCornerShape(12.dp)
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

}


@Composable
fun ThemeSwitchItem(title: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title, modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        RadioButton(selected = selected, onClick = onClick)
    }
}

@Composable
fun getColorForAccent(accentColor: AccentColor, customColor: Color? = null): Color {
    return when (accentColor) {
        AccentColor.DYNAMIC -> getDynamicColorPreview()
        AccentColor.CUSTOM -> Color.Transparent
        AccentColor.MINT -> Color(0xFF00E5A0)
        AccentColor.PURPLE -> Color(0xFF9C27B0)
        AccentColor.ORANGE -> Color(0xFFFF9800)
        AccentColor.BLUE -> Color(0xFF2196F3)
        AccentColor.PINK -> Color(0xFFE91E63)
    }
}

@Composable
fun getDynamicColorPreview(): Color {
    val context = LocalContext.current
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicLightColorScheme(context).primary
    } else {
        Color.Gray
    }
}

@Composable
fun RainbowPreviewCircle(modifier: Modifier = Modifier) {
    Canvas(
        modifier = modifier
            .size(24.dp)
            .clip(CircleShape)
    ) {
        val rainbowBrush = Brush.linearGradient(
            colors = listOf(
                Color.Red,
                Color.Yellow,
                Color.Green,
                Color.Cyan,
                Color.Blue,
                Color.Magenta
            ),
            start = Offset.Zero,
            end = Offset(size.width, size.height)
        )
        drawCircle(brush = rainbowBrush)
    }
}


@Composable
fun CustomColorPickerDialog(
    initialColor: Color = Color.White,
    onDismissRequest: () -> Unit,
    onColorSelected: (Color) -> Unit
) {
    var selectedColor by remember { mutableStateOf(initialColor) }
    val controller = rememberColorPickerController()

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Выберите цвет") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .background(selectedColor)
                        .border(1.dp, Color.Gray)
                )
                Spacer(modifier = Modifier.height(16.dp))

                HsvColorPicker(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    initialColor = selectedColor,
                    onColorChanged = { colorEnvelope ->
                        selectedColor = colorEnvelope.color
                    },
                    controller = controller,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onColorSelected(selectedColor) }) {
                Text(
                    "Выбрать", style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    "Отмена", style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    )
}
