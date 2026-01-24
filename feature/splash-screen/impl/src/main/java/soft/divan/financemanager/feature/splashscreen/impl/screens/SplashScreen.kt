package soft.divan.financemanager.feature.splashscreen.impl.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay
import soft.divan.financemanager.feature.splash_screen.impl.R
import soft.divan.financemanager.uikit.theme.FinanceManagerTheme

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun AboutTheProgramScreenPreview() {
    FinanceManagerTheme {
        SplashContent()
    }
}

private const val SPLASH_DELAY_MS = 1_000L

@Composable
fun SplashScreen(
    onNavigateToExpenses: () -> Unit,
) {
    LaunchedEffect(true) {
        delay(SPLASH_DELAY_MS)
        onNavigateToExpenses()
    }

    SplashContent()
}

@Composable
fun SplashContent() {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.splash_animation))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        speed = 1.0f
    )

    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = Modifier.fillMaxSize(),
    )

}
