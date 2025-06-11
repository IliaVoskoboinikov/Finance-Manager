package soft.divan.financemanager.presenter.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay
import soft.divan.financemanager.R


object SplashScreen {
    const val route = "splash"
}

@Composable
fun SplashScreen(
    navHostController: NavHostController,
) {
    SplashContent()
    LaunchedEffect(true) {
        delay(1000)
        navHostController.navigate(Screen.Expanses.route) {
            popUpTo(SplashScreen.route) { inclusive = true }
        }
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
