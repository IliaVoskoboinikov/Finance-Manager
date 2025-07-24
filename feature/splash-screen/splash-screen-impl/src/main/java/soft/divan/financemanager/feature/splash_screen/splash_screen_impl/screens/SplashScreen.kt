package soft.divan.financemanager.feature.splash_screen.splash_screen_impl.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay
import soft.divan.financemanager.feature.splash_screen.splash_screen_impl.R


@Composable
fun SplashScreen(
    onNavigateToExpenses: () -> Unit,
) {
    SplashContent()
    LaunchedEffect(true) {
        delay(1000)
        onNavigateToExpenses()

    }
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
