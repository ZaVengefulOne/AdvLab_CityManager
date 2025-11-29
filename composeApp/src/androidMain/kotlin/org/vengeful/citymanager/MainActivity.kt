package org.vengeful.citymanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.vengeful.citymanager.di.initKoin
import org.vengeful.citymanager.navigation.AndroidHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            AndroidApp()
        }
    }
}

@Composable
fun AndroidApp() {
    initKoin()
    MaterialTheme {
        AndroidHost()
    }
}
