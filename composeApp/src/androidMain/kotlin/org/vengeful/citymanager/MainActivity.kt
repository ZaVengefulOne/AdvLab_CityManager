package org.vengeful.citymanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import org.koin.core.context.GlobalContext
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.vengeful.citymanager.di.androidModule
import org.vengeful.citymanager.di.appModule
import org.vengeful.citymanager.navigation.AndroidHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Настройка полноэкранного режима
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController?.let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            AndroidApp()
        }
    }
}

@Composable
fun AndroidApp() {
    // Initialize Koin with both common and Android-specific modules
    val koinInitialized = remember {
        try {
            loadKoinModules(
                listOf(androidModule)
            )
        } catch (e: Exception) {
            // First time initialization
            startKoin {
                modules(appModule, androidModule)
            }
        }
        true
    }

    MaterialTheme {
        AndroidHost()
    }
}
