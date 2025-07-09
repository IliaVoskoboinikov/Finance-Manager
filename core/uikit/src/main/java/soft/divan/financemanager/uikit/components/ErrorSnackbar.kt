package soft.divan.financemanager.uikit.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun ErrorSnackbar(
    snackbarHostState: SnackbarHostState,
    message: String,
) {
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }
}


