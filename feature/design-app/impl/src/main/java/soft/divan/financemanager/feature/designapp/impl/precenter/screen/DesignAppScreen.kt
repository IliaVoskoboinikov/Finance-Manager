package soft.divan.financemanager.feature.designapp.impl.precenter.screen

import android.os.Build
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import com.github.skydoves.navgraph.annotations.NavDestination
import com.github.skydoves.navgraph.annotations.NavPreview
import soft.divan.financemanager.feature.design_app.impl.R
import soft.divan.financemanager.feature.designapp.api.DesignAppKey
import soft.divan.financemanager.feature.designapp.impl.domain.model.ThemeMode
import soft.divan.financemanager.feature.designapp.impl.precenter.model.DesignUiState
import soft.divan.financemanager.feature.designapp.impl.precenter.model.mockDesignUiStateSuccess
import soft.divan.financemanager.feature.designapp.impl.precenter.viewModel.DesignAppViewModel
import soft.divan.financemanager.uikit.components.ErrorContent
import soft.divan.financemanager.uikit.components.LoadingProgressBar
import soft.divan.financemanager.uikit.components.TopBar
import soft.divan.financemanager.uikit.model.TopBarModel
import soft.divan.financemanager.uikit.theme.AccentColor
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme

@NavPreview(route = DesignAppKey::class, primary = true)
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun TransactionScreenPreview() {
    FinanceManagerTheme {
        DesignAppContent(
            uiState = mockDesignUiStateSuccess,
            setAccentColor = {},
            onThemeSelect = {},
            setCustomColor = { },
            onRetry = {}
        )
    }
}

@NavDestination(route = DesignAppKey::class)
@Composable
fun DesignAppScreen(
    modifier: Modifier = Modifier,
    viewModel: DesignAppViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DesignAppContent(
        modifier = modifier,
        uiState = uiState,
        setAccentColor = viewModel::setAccentColor,
        onThemeSelect = viewModel::onThemeSelected,
        setCustomColor = viewModel::setCustomColor,
        onRetry = viewModel::retry
    )
}

@Composable
fun DesignAppContent(
    uiState: DesignUiState,
    setAccentColor: (AccentColor) -> Unit,
    onThemeSelect: (ThemeMode) -> Unit,
    setCustomColor: (Color) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopBar(topBar = TopBarModel(title = R.string.design)) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (uiState) {
                is DesignUiState.Error -> ErrorContent(onClick = { onRetry() })

                is DesignUiState.Loading -> LoadingProgressBar()

                is DesignUiState.Success -> DesignAppForm(
                    uiState = uiState,
                    setAccentColor = setAccentColor,
                    onThemeSelect = onThemeSelect,
                    setCustomColor = setCustomColor
                )
            }
        }
    }
}

@Composable
fun DesignAppForm(
    uiState: DesignUiState.Success,
    setAccentColor: (AccentColor) -> Unit,
    onThemeSelect: (ThemeMode) -> Unit,
    setCustomColor: (Color) -> Unit,
    modifier: Modifier = Modifier
) {
    var isShowColorPicker by remember { mutableStateOf(false) }

    ShowColorPicker(isShowColorPicker, { isShowColorPicker = false }, setCustomColor)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ThemeSection(
            selectedTheme = uiState.themeMode,
            onThemeSelect = onThemeSelect
        )

        AccentColorSection(
            currentColor = uiState.accentColor,
            onColorSelect = setAccentColor,
            onCustomClick = { isShowColorPicker = true }
        )
    }
}

@Composable
private fun ShowColorPicker(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    setCustomColor: (Color) -> Unit
) {
    if (isVisible) {
        CustomColorPickerDialog(
            initialColor = Color.Yellow,
            onDismissRequest = onDismiss,
            onColorSelect = { selectedColor ->
                setCustomColor(selectedColor)
                onDismiss()
            }
        )
    }
}

@Composable
fun ThemeSection(
    selectedTheme: ThemeMode,
    onThemeSelect: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.choice_theme),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        ThemeSwitchItem(
            title = stringResource(R.string.light),
            selected = selectedTheme == ThemeMode.LIGHT,
            onClick = { onThemeSelect(ThemeMode.LIGHT) }
        )
        ThemeSwitchItem(
            title = stringResource(R.string.dark),
            selected = selectedTheme == ThemeMode.DARK,
            onClick = { onThemeSelect(ThemeMode.DARK) }
        )
        ThemeSwitchItem(
            title = stringResource(R.string.system),
            selected = selectedTheme == ThemeMode.SYSTEM,
            onClick = { onThemeSelect(ThemeMode.SYSTEM) }
        )
    }
}

@Composable
fun AccentColorSection(
    currentColor: AccentColor,
    onColorSelect: (AccentColor) -> Unit,
    onCustomClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AccentColor.entries
        } else {
            AccentColor.entries.filter { it != AccentColor.DYNAMIC }
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.color_palette),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        colors.forEach { color ->
            if (color == AccentColor.CUSTOM) {
                CustomColorOptionItem(
                    selected = currentColor == AccentColor.CUSTOM,
                    onClick = onCustomClick
                )
            } else {
                ColorOptionItem(
                    accent = color,
                    selected = currentColor == color,
                    onClick = { onColorSelect(color) }
                )
            }
        }
    }
}

@Composable
fun CustomColorOptionItem(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color.Red,
                        Color.Yellow,
                        Color.Green,
                        Color.Cyan,
                        Color.Blue,
                        Color.Magenta
                    )
                )
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Custom",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun ColorOptionItem(
    accent: AccentColor,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val previewColor = getColorForAccent(accent)

    Card(
        modifier = modifier
            .height(56.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = previewColor,
            contentColor = contentColorFor(previewColor)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = accent.name.lowercase().replaceFirstChar { it.titlecase() },
                style = MaterialTheme.typography.bodyLarge
            )
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun ThemeSwitchItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        RadioButton(selected = selected, onClick = onClick)
    }
}

@Composable
fun getColorForAccent(
    accentColor: AccentColor,
    customColor: Color? = null
): Color = when (accentColor) {
    AccentColor.DYNAMIC -> getDynamicColorPreview()

    AccentColor.CUSTOM -> customColor ?: Color.Transparent

    else -> requireNotNull(accentColor.color) {
        "AccentColor ${accentColor.name} must have a color"
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
fun CustomColorPickerDialog(
    onDismissRequest: () -> Unit,
    onColorSelect: (Color) -> Unit,
    initialColor: Color = Color.White
) {
    var selectedColor by remember { mutableStateOf(initialColor) }
    val controller = rememberColorPickerController()

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.choose_color)) },
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
                    controller = controller
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onColorSelect(selectedColor) }) {
                Text(
                    stringResource(R.string.choose),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    stringResource(R.string.сancel),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        }
    )
}
