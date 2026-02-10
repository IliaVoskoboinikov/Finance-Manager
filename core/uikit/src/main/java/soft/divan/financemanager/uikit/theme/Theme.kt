package soft.divan.financemanager.uikit.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

fun baseLightColorScheme(primaryColor: Color): ColorScheme = lightColorScheme(
    primary = primaryColor,
    background = RoseWhite,
    primaryContainer = NeonMint,
    onPrimaryContainer = White,
    surface = primaryColor,
    surfaceContainer = LavenderMist,
    onSecondaryContainer = NeonMint,
    secondaryContainer = MintBackground,
    onSurfaceVariant = Graphite,
    onSurface = CharcoalPurple,
    error = CoralRed,
    surfaceContainerHigh = SoftLavender
)

fun baseDarkColorScheme(primaryColor: Color): ColorScheme = darkColorScheme(
    primary = primaryColor,
    background = BlackDark,
    primaryContainer = MintBackgroundDark,
    onPrimaryContainer = BlackDark,
    surface = primaryColor,
    surfaceContainer = RoseWhiteDark,
    onSecondaryContainer = WhiteDark,
    secondaryContainer = SoftLavenderDark,
    onSurfaceVariant = GraphiteDark,
    onSurface = WhiteDark,
    error = CoralRedDark,
    surfaceContainerHigh = LavenderMistDark
)

@Composable
fun FinanceManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentColor: AccentColor = AccentColor.MINT,
    customColor: Color? = null,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        accentColor == AccentColor.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        accentColor == AccentColor.CUSTOM -> {
            val resolvedColor = customColor ?: Color.Magenta
            if (darkTheme) {
                baseDarkColorScheme(
                    resolvedColor
                )
            } else {
                baseLightColorScheme(resolvedColor)
            }
        }

        else -> {
            val primaryColor = accentColor.color ?: NeonMint
            if (darkTheme) baseDarkColorScheme(primaryColor) else baseLightColorScheme(primaryColor)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
// Revue me>>
