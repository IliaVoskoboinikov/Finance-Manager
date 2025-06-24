package soft.divan.financemanager.presenter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import soft.divan.financemanager.data.util.NetworkMonitor
import soft.divan.financemanager.presenter.ui.screens.MainScreen
import soft.divan.financemanager.presenter.ui.theme.FinanceManagerTheme
import javax.inject.Inject

@AndroidEntryPoint
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
