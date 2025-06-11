package soft.divan.financemanager.presenter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.BlendMode.Companion.Color
import soft.divan.financemanager.presenter.ui.screens.MainScreen
import soft.divan.financemanager.presenter.ui.theme.FinanceManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinanceManagerTheme {
              MainScreen()
            }
        }
    }
}
