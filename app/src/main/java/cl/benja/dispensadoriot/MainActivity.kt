package cl.benja.dispensadoriot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import cl.benja.dispensadoriot.ui.theme.DispensadorIoTTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DispensadorIoTTheme {
                DispensadorApp()
            }
        }
    }
}