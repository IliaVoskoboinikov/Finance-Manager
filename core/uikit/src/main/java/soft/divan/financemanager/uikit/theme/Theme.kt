package soft.divan.financemanager.uikit.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NeonMintDark,
    background = BlackDark,
    primaryContainer = MintBackgroundDark,
    onPrimaryContainer = BlackDark,
    surface = LavenderMistDark,
    surfaceContainer = RoseWhiteDark,
    onSecondaryContainer = WhiteDark,
    secondaryContainer = SoftLavenderDark,
    onSurfaceVariant = GraphiteDark,
    onSurface = WhiteDark,
    error = CoralRedDark,
    surfaceContainerHigh = LavenderMistDark,
)

private val LightColorScheme = lightColorScheme(

    primary = NeonMint,
    background = RoseWhite,
    primaryContainer = NeonMint,
    onPrimaryContainer = White,
    surface = NeonMint,
    surfaceContainer = LavenderMist,
    onSecondaryContainer = NeonMint,
    secondaryContainer = MintBackground,
    onSurfaceVariant = Graphite,
    onSurface = CharcoalPurple,
    error = CoralRed,
    surfaceContainerHigh = SoftLavender

)

val PurpleColorScheme = lightColorScheme(
    primary = Color(0xFF9C27B0),
    background = RoseWhite,
    primaryContainer = NeonMint,
    onPrimaryContainer = White,
    surface = NeonMint,
    surfaceContainer = LavenderMist,
    onSecondaryContainer = NeonMint,
    secondaryContainer = MintBackground,
    onSurfaceVariant = Graphite,
    onSurface = CharcoalPurple,
    error = CoralRed,
    surfaceContainerHigh = SoftLavender
)

val OrangeColorScheme = lightColorScheme(
    primary = Color(0xFFFF9800),
    background = RoseWhite,
    primaryContainer = NeonMint,
    onPrimaryContainer = White,
    surface = NeonMint,
    surfaceContainer = LavenderMist,
    onSecondaryContainer = NeonMint,
    secondaryContainer = MintBackground,
    onSurfaceVariant = Graphite,
    onSurface = CharcoalPurple,
    error = CoralRed,
    surfaceContainerHigh = SoftLavender
)

val BlueColorScheme = lightColorScheme(
    primary = Color(0xFF2196F3),
    background = RoseWhite,
    primaryContainer = NeonMint,
    onPrimaryContainer = White,
    surface = NeonMint,
    surfaceContainer = LavenderMist,
    onSecondaryContainer = NeonMint,
    secondaryContainer = MintBackground,
    onSurfaceVariant = Graphite,
    onSurface = CharcoalPurple,
    error = CoralRed,
    surfaceContainerHigh = SoftLavender
)

val PinkColorScheme = lightColorScheme(
    primary = Color(0xFFE91E63),
    background = RoseWhite,
    primaryContainer = NeonMint,
    onPrimaryContainer = White,
    surface = NeonMint,
    surfaceContainer = LavenderMist,
    onSecondaryContainer = NeonMint,
    secondaryContainer = MintBackground,
    onSurfaceVariant = Graphite,
    onSurface = CharcoalPurple,
    error = CoralRed,
    surfaceContainerHigh = SoftLavender
)

val PurpleDarkColorScheme = darkColorScheme(
    primary = Color(0xFF9C27B0),
    background = BlackDark,
    primaryContainer = MintBackgroundDark,
    onPrimaryContainer = BlackDark,
    surface = LavenderMistDark,
    surfaceContainer = RoseWhiteDark,
    onSecondaryContainer = WhiteDark,
    secondaryContainer = SoftLavenderDark,
    onSurfaceVariant = GraphiteDark,
    onSurface = WhiteDark,
    error = CoralRedDark,
    surfaceContainerHigh = LavenderMistDark,
)

val OrangeDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF9800),
    background = BlackDark,
    primaryContainer = MintBackgroundDark,
    onPrimaryContainer = BlackDark,
    surface = LavenderMistDark,
    surfaceContainer = RoseWhiteDark,
    onSecondaryContainer = WhiteDark,
    secondaryContainer = SoftLavenderDark,
    onSurfaceVariant = GraphiteDark,
    onSurface = WhiteDark,
    error = CoralRedDark,
    surfaceContainerHigh = LavenderMistDark,
)

val BlueDarkColorScheme = darkColorScheme(
    primary = Color(0xFF2196F3),
    background = BlackDark,
    primaryContainer = MintBackgroundDark,
    onPrimaryContainer = BlackDark,
    surface = LavenderMistDark,
    surfaceContainer = RoseWhiteDark,
    onSecondaryContainer = WhiteDark,
    secondaryContainer = SoftLavenderDark,
    onSurfaceVariant = GraphiteDark,
    onSurface = WhiteDark,
    error = CoralRedDark,
    surfaceContainerHigh = LavenderMistDark,
)

val PinkDarkColorScheme = darkColorScheme(
    primary = Color(0xFFE91E63),
    background = BlackDark,
    primaryContainer = MintBackgroundDark,
    onPrimaryContainer = BlackDark,
    surface = LavenderMistDark,
    surfaceContainer = RoseWhiteDark,
    onSecondaryContainer = WhiteDark,
    secondaryContainer = SoftLavenderDark,
    onSurfaceVariant = GraphiteDark,
    onSurface = WhiteDark,
    error = CoralRedDark,
    surfaceContainerHigh = LavenderMistDark,
)


@Composable
fun FinanceManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentColor: AccentColor = AccentColor.MINT,
    content: @Composable () -> Unit
) {

    val colorScheme = when {
        // Dynamic color is available on Android 12+
        accentColor == AccentColor.DYNAMIC && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current

            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        else -> when (accentColor) {
            AccentColor.MINT -> if (darkTheme) DarkColorScheme else LightColorScheme
            AccentColor.PURPLE -> if (darkTheme) PurpleDarkColorScheme else PurpleColorScheme
            AccentColor.ORANGE -> if (darkTheme) OrangeDarkColorScheme else OrangeColorScheme
            AccentColor.BLUE -> if (darkTheme) BlueDarkColorScheme else BlueColorScheme
            AccentColor.PINK -> if (darkTheme) PinkDarkColorScheme else PinkColorScheme
            AccentColor.DYNAMIC -> if (darkTheme) DarkColorScheme else LightColorScheme
        }
    }


    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}